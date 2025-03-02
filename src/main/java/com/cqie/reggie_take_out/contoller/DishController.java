package com.cqie.reggie_take_out.contoller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqie.reggie_take_out.common.R;
import com.cqie.reggie_take_out.dto.DishDto;
import com.cqie.reggie_take_out.entity.Category;
import com.cqie.reggie_take_out.entity.Dish;
import com.cqie.reggie_take_out.mapper.DishMapper;
import com.cqie.reggie_take_out.service.CategoryService;
import com.cqie.reggie_take_out.service.DishFlavorService;
import com.cqie.reggie_take_out.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品Dto（数据传输对象）接受前端的复杂Json转为对象
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> saveDish(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存信息
//        Set key=redisTemplate.keys("dish_*");
//        redisTemplate.delete(key);
        //清理某个分类的菜品缓存
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        //拷贝pageInfo到dishDtoPage中,不拷贝records属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> dishList = pageInfo.getRecords();
        List<DishDto> dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id) {
        DishDto dishWithFlavor = dishService.getDishWithFlavor(id);
        return R.success(dishWithFlavor);
    }

    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto) {
        log.info("修改菜品：{}" + dishDto.toString());

        dishService.updateDishWithFlavor(dishDto);

        //清理所有菜品的缓存信息
//        Set key=redisTemplate.keys("dish_*");
//        redisTemplate.delete(key);
        //清理某个分类的菜品缓存
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("修改成功");

    }

    @PostMapping("/status/{status}")
    public R<String> updateDishStatus(@RequestParam List<Long> ids, @PathVariable Integer status) {
        log.info("修改菜品:{}状态变为:{}", ids, status);
        LinkedList<Dish> dishList = new LinkedList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishList.add(dish);
        }
        dishService.updateDishWithSetmealStatus(dishList);
        //清理所有菜品的缓存信息
        Set key=redisTemplate.keys("dish_*");
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids) {
        log.info("删除菜品id:{}", ids);
        dishService.removeDishWithFlaor(ids);
        return R.success("删除成功");
    }

    //    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        log.info("菜品的分类id:{}",dish.toString());
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//        queryWrapper.eq(dish.getName()!=null,Dish::getName,dish.getName());
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(queryWrapper);
//        return R.success(dishList);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList;
        //获取redis存储键值
        String key="Dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //根据key先去redis中查询缓存数据中是否存在
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果集合不为空，直接将缓存数据返回
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }
        //集合为空，从数据库中获取
        log.info("菜品的分类id:{}", dish.toString());
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.eq(dish.getName() != null, Dish::getName, dish.getName());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);

       dishDtoList=dishList.stream().map(item->{
            Long categoryId = item.getCategoryId();
            DishDto dishDto = new DishDto();
            dishDto=dishService.getDishWithFlavor(item.getId());
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                dishDto.setCategoryName(category.getName());
            }

            return dishDto;

        }).collect(Collectors.toList());
       //并保存到redis中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
