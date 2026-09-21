package com.campus.secondhand.controller;


import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderPayDTO;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.vo.OrderItemVO;
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
    public Result<List<OrderVO>> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        // TODO 获取Token鉴权，需要等待用户模块的完成
        List<OrderVO> newOrders = orderService.createOrder(orderCreateDTO, 1L);
        return Result.success(newOrders);
    }

    /**
     * 获取订单列表（买/卖角色+状态筛选+各状态计数）
     * @return 返回订单列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> getOrders(@ModelAttribute OrderQueryDTO orderQueryDTO) {
        // TODO 获取Token鉴权，需要等待用户模块的完成
        PageResult<OrderVO> orders =  orderService.getOrders(orderQueryDTO,1L);
        return Result.success(orders);
    }

    /**
     * 获取订单详情（仅买卖双方可见）
     * @param orderId 对应的订单详情id
     * @return 返回对应的订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable("id") Long orderId) {
        // TODO 获取Token鉴权，需要等待用户模块的完成
        OrderVO orderVO = orderItemService.getOrderDetail(orderId, 1L);
        return Result.success(orderVO);
    }

    /**
     * 模拟支付（仅买家，仅待支付）
     * @param orderPayDTO 前端传递的支付方式
     * @param orderId 需要支付的订单id
     * @return 返回Result结果
     */
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@RequestBody OrderPayDTO orderPayDTO, @PathVariable("id") Long orderId) {
        // TODO 获取Token鉴权，需要等待用户模块的完成
        orderService.payOrder(orderPayDTO,orderId,1L);
        return Result.success();
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

    /**
     * 确认收货
     * @param id
     * @return
     */
    @PostMapping("/{id}/confirm")
    public Result confirmOrder(@PathVariable Integer id) {
        return null;
    }

    /**
     * 取消收获
     * @param id
     * @return
     */
    @PostMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Integer id) {
        return null;
    }
}
