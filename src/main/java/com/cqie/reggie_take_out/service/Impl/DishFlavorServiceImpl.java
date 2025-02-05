package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.entity.DishFlavor;
import com.cqie.reggie_take_out.mapper.DishFlavorMapper;
import com.cqie.reggie_take_out.service.DishFlavorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor>
    implements DishFlavorService {
}
