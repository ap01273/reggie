package com.cqie.reggie_take_out.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqie.reggie_take_out.entity.AddressBook;
import com.cqie.reggie_take_out.mapper.AddressBookMapper;
import com.cqie.reggie_take_out.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class AddressBookServiceImpl extends  ServiceImpl<AddressBookMapper,AddressBook>
implements AddressBookService {

}
