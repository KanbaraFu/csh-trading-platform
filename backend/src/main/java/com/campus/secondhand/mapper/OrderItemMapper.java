package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.secondhand.vo.OrderVO;
import org.apache.ibatis.annotations.Param;

/**
* @author Lenovo
* @description 针对表【order_item(订单明细)】的数据库操作Mapper
* @createDate 2026-09-16 11:56:41
* @Entity com.campus.secondhand.pojo.OrderItem
*/
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    OrderVO selectOrderItemById(@Param("orderId") Long orderId);
}




