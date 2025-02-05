package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.entity.User;
import com.cqie.reggie_take_out.mapper.UserMapper;
import com.cqie.reggie_take_out.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper, User> implements UserService {
}
