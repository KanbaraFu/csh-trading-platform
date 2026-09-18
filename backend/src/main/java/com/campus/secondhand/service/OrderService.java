package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【order(订单)】的数据库操作Service
* @createDate 2026-09-16 11:56:41
*/
public interface OrderService extends IRepository<Order> {
    PageResult<OrderVO> getOrders(OrderQueryDTO orderQueryDTO, Long userId);
}
