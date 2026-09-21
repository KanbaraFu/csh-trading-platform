package com.campus.secondhand.vo;

import lombok.Data;

/**
 * 个人中心各项统计（序列化后为 order_total / pending_pay / favorite_count / cart_count / unread_count）
 */
@Data
public class UserStatsVO {

    /**
     * 我的订单总数
     */
    private Long orderTotal;

    /**
     * 待付款订单数
     */
    private Long pendingPay;

    /**
     * 我的收藏数
     */
    private Long favoriteCount;

    /**
     * 购物车条目数
     */
    private Long cartCount;

    /**
     * 未读消息数
     */
    private Long unreadCount;

}
