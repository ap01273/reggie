package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.common.CustomException;
import com.cqie.reggie_take_out.dto.SetmealDto;
import com.cqie.reggie_take_out.entity.Dish;
import com.cqie.reggie_take_out.entity.Setmeal;
import com.cqie.reggie_take_out.entity.SetmealDish;
import com.cqie.reggie_take_out.mapper.SetmealMapper;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
implements SetmealService {
    @Autowired
    private  SetmealDishService setmealDishService;



    /**
     * 新增套餐并保存菜品和套餐的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息,
        this.save(setmealDto);
        //完成新增操作后，套餐id会自动回填到setmealDto中的id上；

        //为菜品设置所关联的套餐id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
           item.setSetmealId(setmealDto.getId());
           return item;
        }).collect(Collectors.toList());
        //保存菜品和套餐的关联关系
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getWithDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    @Override
    public void updateSetmealWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(deleteWrapper);
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);

    }

    /**
     * 删除套餐，同时删除套餐和菜品关联表中的数据
     * @param ids
     */
    @Transactional
    public void removeSetmealWithDish(List<Long> ids) {
        //判断套餐的状态，若为停售，不可删除，抛出异常
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        long count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("套餐启售中，无法删除");
        }
        //可以删除，先删除套餐表的数据
        this.removeByIds(ids);
        //再删除套餐菜品关联表中的信息
        LambdaQueryWrapper<SetmealDish> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(deleteWrapper);
    }
}
