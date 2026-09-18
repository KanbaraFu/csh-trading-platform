package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.mapper.OrderItemMapper;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【order_item(订单明细)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:41
*/
@Service
public class OrderItemServiceImpl extends CrudRepository<OrderItemMapper, OrderItem>
    implements OrderItemService{

    @Override
    public Result<OrderVO> getOrderDetail(Integer id) {

        return null;
    }
}




