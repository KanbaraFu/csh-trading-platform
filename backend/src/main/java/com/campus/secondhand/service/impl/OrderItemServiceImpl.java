package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.mapper.OrderItemMapper;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【order_item(订单明细)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:41
*/
@Service
public class OrderItemServiceImpl extends CrudRepository<OrderItemMapper, OrderItem>
    implements OrderItemService{

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Result<OrderVO> getOrderDetail(Long orderId, Long userId) {
        OrderVO orderVO = orderItemMapper.selectOrderItemById(orderId);
        if (orderVO == null) {
            String message = "订单不存在！";
            throw BizException.notFound(message);
        }

        if ((!orderVO.getBuyerId().equals(userId) && !orderVO.getSellerId().equals(userId))) {
            String message = "抱歉，您没有权限查看此订单详情！";
            throw BizException.badRequest(message);
        }

        return Result.success("订单详情获取成功！",orderVO);
    }
}




