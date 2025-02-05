package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.common.CustomException;
import com.cqie.reggie_take_out.dto.DishDto;
import com.cqie.reggie_take_out.entity.Dish;
import com.cqie.reggie_take_out.entity.DishFlavor;
import com.cqie.reggie_take_out.entity.Setmeal;
import com.cqie.reggie_take_out.entity.SetmealDish;
import com.cqie.reggie_take_out.mapper.DishMapper;
import com.cqie.reggie_take_out.service.DishFlavorService;
import com.cqie.reggie_take_out.service.DishService;
import com.cqie.reggie_take_out.service.SetmealDishService;
import com.cqie.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
        implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增菜品同时保存对应口味
     * @param dishDto
     */
    //事务管理注解
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);

        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getDishWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    public void updateDishWithFlavor(DishDto dishDto) {
        //更新菜品信息
        this.updateById(dishDto);
        //删除原有的口味信息
        LambdaQueryWrapper<DishFlavor> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(deleteWrapper);
        //新增口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 删除菜品，并删除菜品和口味的关联表数据
     * @param ids
     */
    @Transactional
    public void removeDishWithFlaor(List<Long> ids) {
        //检查菜品的状态，若为启售状态，不可删除，抛出异常
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus,1);
        long count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("菜品启售中，无法删除");
        }
        //可以删除，先删除菜品信息
        this.removeByIds(ids);
        //删除菜品和口味关联表中的信息
        LambdaQueryWrapper<DishFlavor> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(DishFlavor::getDishId, ids);

        dishFlavorService.remove(deleteWrapper);
    }

    /**
     * 修改菜品状态，若菜品停售，则对应的套餐也应停售
     *
     * @param dishList
     */
    @Transactional
    public void updateDishWithSetmealStatus(List<Dish> dishList) {
        //修改菜品状态
        this.updateBatchById(dishList);
        //判断状态为停售，则停售对应套餐
        for (Dish dish : dishList) {
            if(dish.getStatus()==0)
            {
                LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SetmealDish::getDishId, dish.getId());
                List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
                List<Setmeal> setmealList = setmealDishList.stream().map((item) -> {
                    Setmeal setmeal = new Setmeal();
                    setmeal.setId(item.getSetmealId());
                    setmeal.setStatus(0);
                    return setmeal;
                }).collect(Collectors.toList());
               setmealService.updateBatchById(setmealList);
            }
        }
    }
}
