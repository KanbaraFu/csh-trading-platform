package com.campus.secondhand.utils;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.CreateMessageDto;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageUtil {

    private static final Logger log = LoggerFactory.getLogger(SendMessageUtil.class);

    @Autowired
    private MessageService messageServiceImpl;

    @Autowired
    private ProductMapper productMapper;

    public void notifyComment(Long productId, Long senderId, Long replyToId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) return;

            Long sellerId = product.getSellerId();
            String productTitle = product.getTitle();

            if (!senderId.equals(sellerId)) {
                sendMessage(sellerId, "comment",
                        "商品被评论",
                        "有人对你的商品「" + productTitle + "」发表了评论",
                        productId);
            }

            if (replyToId != null
                    && !replyToId.equals(senderId)
                    && !replyToId.equals(sellerId)) {
                sendMessage(replyToId, "comment",
                        "评论被回复",
                        "你在商品「" + productTitle + "」下的评论收到了新回复",
                        productId);
            }
        } catch (Exception e) {
            log.warn("发送评论通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    public void notifyOrderStatus(Long orderId, Long buyerId, Long sellerId, int orderStatus) {
        try {
            String title;
            String content;
            String type = "trade";

            switch (orderStatus) {
                case Constants.ORDER_STATUS_PAID:
                    title = "订单已支付";
                    content = "你的订单已完成支付，请等待卖家发货";
                    sendMessage(buyerId, type, title, content, orderId);
                    break;

                case Constants.ORDER_STATUS_DELIVERED:
                    title = "订单已发货";
                    content = "你的订单已发货，请注意查收";
                    sendMessage(buyerId, type, title, content, orderId);
                    break;

                case Constants.ORDER_STATUS_FINISHED:
                    title = "交易已完成";
                    content = "订单已确认完成，感谢你的使用";
                    sendMessage(buyerId, type, title, content, orderId);
                    sendMessage(sellerId, type, "订单交易完成",
                            "你的商品已售出并完成交易", orderId);
                    break;

                case Constants.ORDER_STATUS_CANCELED:
                    title = "订单已取消";
                    content = "订单已取消，商品已恢复上架";
                    sendMessage(buyerId, type, title, content, orderId);
                    sendMessage(sellerId, type, "订单已取消",
                            "买家已取消订单，商品已恢复上架", orderId);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("发送订单通知失败，不影响主流程：{}", e.getMessage());
        }
    }

    private void sendMessage(Long userId, String type, String title, String content, Long bizId) {
        CreateMessageDto dto = new CreateMessageDto();
        dto.setUserId(userId);
        dto.setType(type);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setBizId(bizId);
        messageServiceImpl.createMessage(dto);
    }
}