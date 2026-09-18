package com.campus.secondhand.mapper;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.service.OrderItemService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单数据访问层测试
 * @since 2026-09-17
 */
@SpringBootTest
public class OrderMapperTests {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;


    @Test
    public void testSelectOrderList() {
        OrderQueryDTO orderQueryDTO = new OrderQueryDTO();
        orderQueryDTO.setPageNum(1L);
        orderQueryDTO.setPageSize(10L);
        orderQueryDTO.setStatus("all");
        orderQueryDTO.setRole("buyer");
        // 当前页码，如果没有参数就为默认值
        Long pageNum = orderQueryDTO.getPageNum() != null ? orderQueryDTO.getPageNum() : Constants.DEFAULT_PAGE_NUM;
        // 每页条数，如果没有参数就为默认值
        Long pageSize = orderQueryDTO.getPageSize() != null ? orderQueryDTO.getPageSize() : Constants.DEFAULT_PAGE_SIZE;

        //// 获取分页后的订单id列表
        //IPage<Long> orderIds = orderMapper.selectOrderIdsByPage(page, orderQueryDTO,userId);
        //
        //// 通过订单ids查询订单列表及其订单详情
        //IPage<OrderVO> result = orderMapper.selectOrderListByOrderIds(orderIds.getRecords());
        //
        //// 获取分页结果
        //List<OrderVO> orderVOS = result.getRecords();
        //orderVOS.forEach((orderVO) -> orderVO.setItemCount(orderVO.getItems().size()));
        //orderVOS.forEach((orderVO) -> System.out.println("订单信息为：" + orderVO));
    }
}
