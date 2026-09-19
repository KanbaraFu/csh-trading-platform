// 文件路径: E:\csh-trading-platform-check\backend\src\main\java\com\campus\secondhand\service\MessageNotifyService.java
package com.campus.secondhand.service;

/**
 * （评论与订单创建时）发送消息
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
     * 发送订单状态变更通知
     *
     * @param orderId     订单id
     * @param buyerId     买家id
     * @param sellerId    卖家id
     * @param orderStatus 新状态值（见 Constants.ORDER_STATUS_*）
     */
    void notifyOrderStatus(Long orderId, Long buyerId, Long sellerId, int orderStatus);
}