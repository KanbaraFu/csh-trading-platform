package com.campus.secondhand.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 个人中心统计 Mapper：轻量化聚合各表条数，不额外建立实体。
 * 注意 order 是 MySQL 保留字，需用反引号包裹。
 */
public interface UserStatsMapper {

    /**
     * 我的订单（买家视角）总数
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE buyer_id = #{userId}")
    long countOrders(@Param("userId") Long userId);

    /**
     * 待付款订单数（status = 0）
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE buyer_id = #{userId} AND status = 0")
    long countPendingPay(@Param("userId") Long userId);

    /**
     * 我的收藏数
     */
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId}")
    long countFavorites(@Param("userId") Long userId);

    /**
     * 购物车条目数
     */
    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    long countCartItems(@Param("userId") Long userId);

    /**
     * 未读消息数（is_read = 0）
     */
    @Select("SELECT COUNT(*) FROM message WHERE user_id = #{userId} AND is_read = 0")
    long countUnreadMessages(@Param("userId") Long userId);

}
