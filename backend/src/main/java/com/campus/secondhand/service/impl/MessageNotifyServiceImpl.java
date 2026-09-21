package com.campus.secondhand.service.impl;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.CreateMessageDto;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.service.MessageNotifyService;
import com.campus.secondhand.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息通知服务实现
 */
@Service
public class MessageNotifyServiceImpl implements MessageNotifyService {

    private static final Logger log = LoggerFactory.getLogger(MessageNotifyServiceImpl.class);

    private static final String TYPE_COMMENT = "comment";
    private static final String TYPE_TRADE = "trade";
    private static final String TYPE_SYSTEM = "system";

    @Autowired
    private MessageService messageServiceImpl;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void notifyComment(Long productId, Long senderId, Long replyToId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) return;

            Long sellerId = product.getSellerId();
            String productTitle = product.getTitle();

            if (!senderId.equals(sellerId)) {
                sendMessage(sellerId, TYPE_COMMENT,
                        "商品被评论",
                        "有人对你的商品「" + productTitle + "」发表了评论",
                        productId);
            }

            if (replyToId != null
                    && !replyToId.equals(senderId)
                    && !replyToId.equals(sellerId)) {
                sendMessage(replyToId, TYPE_COMMENT,
                        "评论被回复",
                        "你在商品「" + productTitle + "」下的评论收到了新回复",
                        productId);
            }
        } catch (Exception e) {
            log.warn("发送评论通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    @Override
    public void notifyOrderStatus(Long orderId, Long buyerId, Long sellerId, int orderStatus) {
        try {
            switch (orderStatus) {
                case Constants.ORDER_STATUS_UNPAID:
                    sendMessage(buyerId, TYPE_TRADE, "订单待支付",
                            "你的订单已创建，请尽快完成支付", orderId);
                    sendMessage(sellerId, TYPE_TRADE, "收到新订单",
                            "有人对你的商品下了订单，等待买家付款", orderId);
                    break;

                case Constants.ORDER_STATUS_PAID:
                    sendMessage(buyerId, TYPE_TRADE, "订单已支付",
                            "你的订单已完成支付，请等待卖家发货", orderId);
                    sendMessage(sellerId, TYPE_TRADE, "订单已支付",
                            "你的商品已被购买，请及时发货", orderId);
                    break;

                case Constants.ORDER_STATUS_DELIVERED:
                    sendMessage(buyerId, TYPE_TRADE, "订单已发货",
                            "你的订单已发货，请注意查收", orderId);
                    sendMessage(sellerId, TYPE_TRADE, "订单已发货",
                            "你的订单已发货，请等待买家确认收货", orderId);
                    break;

                case Constants.ORDER_STATUS_FINISHED:
                    sendMessage(buyerId, TYPE_TRADE, "交易已完成",
                            "订单已确认完成，感谢你的使用", orderId);
                    sendMessage(sellerId, TYPE_TRADE, "交易已完成",
                            "你的商品已售出并完成交易", orderId);
                    break;

                case Constants.ORDER_STATUS_CANCELED:
                    sendMessage(buyerId, TYPE_TRADE, "订单已取消",
                            "订单已取消，商品已恢复上架", orderId);
                    sendMessage(sellerId, TYPE_TRADE, "订单已取消",
                            "买家已取消订单，商品已恢复上架", orderId);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("发送订单通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    @Override
    public void notifyOrderTimeout(Long orderId, Long buyerId, Long sellerId) {
        try {
            sendMessage(buyerId, TYPE_TRADE, "订单已超时",
                    "你的订单因未及时支付已自动取消", orderId);
            sendMessage(sellerId, TYPE_TRADE, "订单已超时",
                    "买家的订单因未支付已自动取消，商品已恢复上架", orderId);
        } catch (Exception e) {
            log.warn("发送订单超时通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    @Override
    public void notifySystem(Long userId, String title, String content) {
        try {
            sendMessage(userId, TYPE_SYSTEM, title, content, null);
        } catch (Exception e) {
            log.warn("发送系统通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    @Override
    public void notifySystemBatch(List<Long> userIds, String title, String content) {
        if (userIds == null || userIds.isEmpty()) return;
        for (Long userId : userIds) {
            notifySystem(userId, title, content);
        }
    }

    private void sendMessage(Long userId, String type, String title, String content, Long bizId) {
        if (userId == null) return;
        CreateMessageDto dto = new CreateMessageDto();
        dto.setUserId(userId);
        dto.setType(type);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setBizId(bizId);
        messageServiceImpl.createMessage(dto);
    }
}