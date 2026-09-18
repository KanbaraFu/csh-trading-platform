package com.campus.secondhand.service;


import com.alibaba.fastjson.JSON;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单服务测试类
 * @since 2026-09-17
 */
@SpringBootTest
public class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Test
    public void testGetOrders() {
        OrderQueryDTO orderQueryDTO = new OrderQueryDTO();
        orderQueryDTO.setPageNum(1L);
        orderQueryDTO.setPageSize(5L);
        orderQueryDTO.setStatus("all");
        orderQueryDTO.setRole("buyer");
        PageResult<OrderVO> result = orderService.getOrders(orderQueryDTO,1L);
        List<OrderVO> orderVOS = result.getRecords();
        orderVOS.forEach((orderVO) -> System.out.println("订单信息为：" + orderVO));
        System.out.println(result.getPages());
        System.out.println(result.getTotal());
        System.out.println(JSON.toJSON(result));
    }
}
