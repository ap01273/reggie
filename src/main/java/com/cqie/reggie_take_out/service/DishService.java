package com.cqie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqie.reggie_take_out.dto.DishDto;
import com.cqie.reggie_take_out.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);

//    public void updateWithFlavor(DishDto dishDto);

    public DishDto getDishWithFlavor(Long id);

    public void updateDishWithFlavor(DishDto dishDto);

    public void removeDishWithFlaor(List<Long> ids);

    /**
     * 修改菜品状态，若菜品停售，则对应的套餐也应停售
     * @param dishList
     */
    public void updateDishWithSetmealStatus(List<Dish> dishList);
}
