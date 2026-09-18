package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【order(订单)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:41
*/
@Service
public class OrderServiceImpl extends CrudRepository<OrderMapper, Order>
    implements OrderService{

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Override
    public PageResult<OrderVO> getOrders(OrderQueryDTO orderQueryDTO, Long userId) {
        // 当前页码，如果没有参数就为默认值
        Long pageNum = orderQueryDTO.getPageNum() != null ? orderQueryDTO.getPageNum() : Constants.DEFAULT_PAGE_NUM;
        // 每页条数，如果没有参数就为默认值
        Long pageSize = orderQueryDTO.getPageSize() != null ? orderQueryDTO.getPageSize() : Constants.DEFAULT_PAGE_SIZE;

        // 开启分页
        Page<OrderVO> page = new Page<>(pageNum,pageSize);

        // 获取分页后的订单id列表
        IPage<Long> orderIds = orderMapper.selectOrderIdsByPage(page, orderQueryDTO,userId);

        // 通过订单ids查询订单列表及其订单详情
        List<OrderVO> orderVOS = orderMapper.selectOrderListByOrderIds(orderIds.getRecords());

        // 获取订单中的商品数量，并将其设置在vo对象上的itemCount上
        orderVOS.forEach((orderVO) -> orderVO.setItemCount(orderVO.getItems().size()));

        // 获取订单id对应的status值（需要获取每个状态的数量来放进counts里）

        List<Map<String, Long>> results = orderMapper.selectAllStatus(orderQueryDTO,userId);
        Map<String, Long> orderCounts = new HashMap<>();

        Long counts = 0L;
        for (Map<String, Long> result : results) {
            // 当前状态的订单数量
            Long count = result.get("count");
            orderCounts.put(String.valueOf(result.get("status")), count);
            counts += count;
        }

        orderCounts.put("all", counts);

        return PageResult.of(page,orderVOS,orderCounts);
    }
}




