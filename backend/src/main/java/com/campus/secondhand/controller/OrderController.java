package com.campus.secondhand.controller;


import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单控制层
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 创建订单
     * @return 返回创建的结果
     */
    @PostMapping
    public Result createOrder() {
        return null;
    }

    /**
     * 获取订单列表（买/卖角色+状态筛选+各状态计数）
     * @return 返回一个订单列表
     */
    @GetMapping
    public PageResult<OrderVO> getOrders(@ModelAttribute OrderQueryDTO orderQueryDTO) {
        System.out.println("orderQueryDTO:"+orderQueryDTO);
        return orderService.getOrders(orderQueryDTO,1L);
    }

    /**
     * 获取订单详情（仅买卖双方可见）
     * @param id 对应的订单详情id
     * @return 返回对应的订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Integer id) {
        return null;
    }

    /**
     * 模拟支付（仅买家，仅待支付）
     * @param id
     * @return
     */
    @PostMapping("/{id}/pay")
    public Result payOrder(@PathVariable Integer id) {
        return null;
    }

    /**
     * 发货（仅已支付）
     * @param id
     * @return
     */
    @PostMapping("/{id}/ship")
    public Result shipOrder(@PathVariable Integer id) {
        return null;
    }

    @PostMapping("/{id}/confirm")
    public Result confirmOrder(@PathVariable Integer id) {
        return null;
    }

    @PostMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Integer id) {
        return null;
    }
}
