package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.common.CustomException;
import com.cqie.reggie_take_out.entity.Category;
import com.cqie.reggie_take_out.entity.Dish;
import com.cqie.reggie_take_out.entity.Setmeal;
import com.cqie.reggie_take_out.mapper.CategoryMapper;
import com.cqie.reggie_take_out.service.CategoryService;
import com.cqie.reggie_take_out.service.DishService;
import com.cqie.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    @Autowired
    public DishService dishService;
    @Autowired
    public SetmealService setmealService;
    /**
     * 根据id删除分类，在删除之前需要进行判断
     * @param id
     */
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();

        //查询当前分类是否关联了菜品,如果已经关联,抛出一个业务异常
        dishWrapper.eq(Dish::getCategoryId, id);
        long count = dishService.count(dishWrapper);

        if (count > 0) {
            //说明已经关联菜品
            throw new CustomException("当前分类下关联了菜品，无法删除");
        }
        //查询当前分类是否关联了套餐,如果已经关联,抛出一个业务异常
        setmealWrapper.eq(Setmeal::getCategoryId, id);
        long count1 = setmealService.count(setmealWrapper);
        if (count1 > 0) {
            //已经关联套餐
            throw new CustomException("当前分类关联了套餐，无法删除");
        }
        //正常删除分类
        super.removeById(id);
    }
}
