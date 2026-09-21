package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderPayDTO;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【order(订单)】的数据库操作Service
* @createDate 2026-09-16 11:56:41
*/
public interface OrderService extends IRepository<Order> {
    /**
     * 创建订单
     * @param orderCreateDTO 前端传递的参数
     * @param userId 当前登录的用户id（鉴权,这里也可以理解为买家id）
     * @return 返回订单
     */
    List<OrderVO> createOrder(OrderCreateDTO orderCreateDTO, Long userId);

    /**
     * 获取订单列表
     * @param orderQueryDTO 前端传递的查询订单列表的参数
     * @param userId 当前登录的用户id（鉴权）
     * @return 返回订单列表
     */
    PageResult<OrderVO> getOrders(OrderQueryDTO orderQueryDTO, Long userId);

    /**
     * 支付订单
     * @param orderPayDTO 前端传递的支付方式
     * @param orderId 需要支付的订单id
     * @param userId 当前登录的用户id（鉴权,这里也可以理解为买家id）
     */
    void payOrder(OrderPayDTO orderPayDTO, Long orderId, Long userId);

    /**
     * 订单发货
     * @param orderId 需要发货的订单id
     * @param userId 当前登录的用户id（鉴权，这里也可以理解为卖家id）
     */
    void shipOrder(Long orderId, Long userId);
}
