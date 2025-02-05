package com.cqie.reggie_take_out.contoller;

import com.cqie.reggie_take_out.common.BaseContext;
import com.cqie.reggie_take_out.common.R;
import com.cqie.reggie_take_out.entity.Orders;
import com.cqie.reggie_take_out.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info(orders.toString());
        orderService.submit(orders);
        return R.success("成功");
    }
}
