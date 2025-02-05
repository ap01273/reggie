package com.cqie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqie.reggie_take_out.dto.SetmealDto;
import com.cqie.reggie_take_out.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveSetmealWithDish(SetmealDto setmealDto);

    public SetmealDto getWithDish(Long id);

    public void updateSetmealWithDish(SetmealDto setmealDto);

    public void removeSetmealWithDish(List<Long> ids);
}
