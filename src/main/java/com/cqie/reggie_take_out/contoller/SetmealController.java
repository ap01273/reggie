package com.cqie.reggie_take_out.contoller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqie.reggie_take_out.common.R;
import com.cqie.reggie_take_out.dto.SetmealDto;
import com.cqie.reggie_take_out.entity.Category;
import com.cqie.reggie_take_out.entity.Dish;
import com.cqie.reggie_take_out.entity.Setmeal;
import com.cqie.reggie_take_out.entity.SetmealDish;
import com.cqie.reggie_take_out.service.CategoryService;
import com.cqie.reggie_take_out.service.SetmealDishService;
import com.cqie.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setmealCache" ,allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
            log.info("添加套餐信息：{}",setmealDto.toString());
            setmealService.saveSetmealWithDish(setmealDto);
            return R.success("成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name) {
        //Setmeal套餐类中没有套餐分类名（dto中有），只有分类id，但是前端需要分类名
        //所有我们需要通过手动设置dto中的套餐分类名，然后返回给前端使用
            Page<Setmeal> pageInfo = new Page<>(page,pageSize);
            Page<SetmealDto> dtoPage=new Page<>(page,pageSize);
            //查询套餐信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);
        //将套餐信息分页查询的结果复制给dtoPage,不包含records，等我们修改好record后在设置records
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        //从pageInfo中去除records集合
        List<Setmeal> records = pageInfo.getRecords();
        //将集合中的Setmeal的属性复制给SetmealDto，并根据Setmeal中的分类id去查找对应的分类名称
        //然后赋值给Dto中的分类名，转为新的dto集合，设置到dtoPage中,返回
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(setmealDtoList);
        return R.success(dtoPage);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        log.info("修改套餐的id:{}",id);
       SetmealDto setmealDto = setmealService.getWithDish(id);
       return R.success(setmealDto);
    }

    @PutMapping
    public R<String> updateWithDish(@RequestBody SetmealDto setmealDto) {
        log.info("修改套餐信息{}",setmealDto.toString());
        setmealService.updateSetmealWithDish(setmealDto);
        return R.success("修改成功");
    }

    @PostMapping("/status/{status}")
    public R<String> updateSetmealStatus(@PathVariable Integer status,Long[] ids)  {
        log.info("修改套餐id：{}的状态：{}",ids.toString(),status);
        List<Setmeal> list = new LinkedList<>();
        for (Long l :ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(l);
            setmeal.setStatus(status);
            list.add(setmeal);
        }
        setmealService.updateBatchById(list);
        return R.success("修改成功");
    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache" ,allEntries = true)
    public R<String> deleteSetmeal(@RequestParam List<Long> ids) {
        log.info("删除套餐id:{}",ids);
        setmealService.removeSetmealWithDish(ids);
        return R.success("套餐数据删除成功");
    }
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }
}
