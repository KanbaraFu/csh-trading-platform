package com.campus.secondhand.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.secondhand.vo.OrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
* @author Lenovo
* @description 针对表【order(订单)】的数据库操作Mapper
* @createDate 2026-09-16 11:56:41
* @Entity com.campus.secondhand.pojo.Order
*/
public interface OrderMapper extends BaseMapper<Order> {

    // 分页查询订单id
    IPage<Long> selectOrderIdsByPage(IPage<OrderVO> page, @Param("query") OrderQueryDTO orderQueryDTO, @Param("userId") Long userId);

    // 通过订单id查询订单列表及每个订单的详情
    List<OrderVO> selectOrderListByOrderIds(List<Long> orderIds);

    // 查询各个状态订单详情
    @Select("SELECT status, COUNT(*) AS `count` FROM `order` GROUP BY status ORDER BY status")
    List<Map<String,Long>> selectAllStatus(@Param("query") OrderQueryDTO orderQueryDTO, @Param("userId") Long userId);
}


