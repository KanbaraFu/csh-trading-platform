package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.vo.OrderVO;

/**
* @author Lenovo
* @description 针对表【order_item(订单明细)】的数据库操作Service
* @createDate 2026-09-16 11:56:41
*/
public interface OrderItemService extends IRepository<OrderItem> {
    Result<OrderVO> getOrderDetail(Integer id);
}
