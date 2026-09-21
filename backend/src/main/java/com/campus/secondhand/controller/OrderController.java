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
import com.campus.secondhand.utils.TokenUtil;
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

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 创建订单
     * @return 返回创建的结果
     */
    @PostMapping
    public Result<List<OrderVO>> createOrder(@RequestBody OrderCreateDTO orderCreateDTO,
                                             @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            List<OrderVO> newOrders = orderService.createOrder(orderCreateDTO, userId);
            return Result.success(newOrders);
        } else {
            return Result.error("请先登录！");
        }
    }

    /**
     * 获取订单列表（买/卖角色+状态筛选+各状态计数）
     * @return 返回订单列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> getOrders(@ModelAttribute OrderQueryDTO orderQueryDTO,
                                                 @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            PageResult<OrderVO> orders =  orderService.getOrders(orderQueryDTO,userId);
            return Result.success(orders);
        } else {
            return Result.error("请先登录！");
        }
    }

    /**
     * 获取订单详情（仅买卖双方可见）
     * @param orderId 对应的订单详情id
     * @return 返回对应的订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable("id") Long orderId,
                                          @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            OrderVO orderVO = orderItemService.getOrderDetail(orderId, userId);
            return Result.success(orderVO);
        } else {
            return Result.error("请先登录！");
        }
    }

    /**
     * 模拟支付（仅买家，仅待支付）
     * @param orderPayDTO 前端传递的支付方式
     * @param orderId 需要支付的订单id
     * @return 返回Result结果
     */
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@RequestBody OrderPayDTO orderPayDTO, @PathVariable("id") Long orderId,
                                 @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            orderService.payOrder(orderPayDTO,orderId,userId);
            return Result.success();
        } else {
            return Result.error("请先登录！");
        }
    }

    /**
     * 发货（仅已支付）
     * @param orderId 需要发货的订单id
     * @return 返回Result结果
     */
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable("id") Long orderId,
                                  @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            orderService.shipOrder(orderId,userId);
            return Result.success();
        } else {
            return Result.error("请先登录！");
        }

    }

    /**
     * 确认收货
     * @param orderId 需要确认收货的订单id
     * @return 返回Result结果
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable("id") Long orderId,
                                     @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            orderService.confirmOrder(orderId,userId);
            return Result.success();
        } else {
            return Result.error("请先登录！");
        }

    }

    /**
     * 取消收货
     * @param orderId 需要取消收货的订单id
     * @return 返回Result结果
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable("id") Long orderId,
                                    @SessionAttribute(value = "token", required = false) String token) {
        if (tokenUtil.validate(token)) {
            Long userId = tokenUtil.getUserId(token);
            orderService.cancelOrder(orderId,userId);
            return Result.success();
        } else {
            return Result.error("请先登录！");
        }
    }
}
