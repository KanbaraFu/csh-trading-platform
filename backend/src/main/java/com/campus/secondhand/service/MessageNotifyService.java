package com.campus.secondhand.service;

import java.util.List;

/**
 * 消息通知服务
 * 供评论模块、订单模块等调用，自动创建站内消息通知。
 * 所有方法内部做了防御处理，通知发送失败不会影响主业务流程。
 */
public interface MessageNotifyService {

    /**
     * 发送评论通知
     * - 顶层评论：通知商品卖家
     * - 回复评论：通知被回复者 + 商品卖家（去重）
     *
     * @param productId 商品id
     * @param senderId  评论发起者id
     * @param replyToId 被回复者id（顶层评论传 null）
     */
    void notifyComment(Long productId, Long senderId, Long replyToId);

    /**
     * 发送订单状态变更通知（通知买卖双方）
     * 覆盖全部状态：待支付 / 已支付 / 已发货 / 已完成 / 已取消
     *
     * @param orderId     订单id
     * @param buyerId     买家id
     * @param sellerId    卖家id
     * @param orderStatus 状态值（见 Constants.ORDER_STATUS_*）
     */
    void notifyOrderStatus(Long orderId, Long buyerId, Long sellerId, int orderStatus);

    /**
     * 发送订单超时未支付通知（通知买卖双方）
     *
     * @param orderId  订单id
     * @param buyerId  买家id
     * @param sellerId 卖家id
     */
    void notifyOrderTimeout(Long orderId, Long buyerId, Long sellerId);

    /**
     * 发送系统通知（面向单个用户）
     *
     * @param userId  接收者id
     * @param title   通知标题
     * @param content 通知内容
     */
    void notifySystem(Long userId, String title, String content);

    /**
     * 发送系统通知（面向多个用户）
     *
     * @param userIds 接收者id列表
     * @param title   通知标题
     * @param content 通知内容
     */
    void notifySystemBatch(List<Long> userIds, String title, String content);
}