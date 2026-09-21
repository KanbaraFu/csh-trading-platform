package com.campus.secondhand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建信息时接收的参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageDto {

    /**
     * 接收者用户id
     */
    private Long userId;

    /**
     * 消息类型：trade交易 / comment评论 / system系统
     */
    private String type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联业务id（订单id（trade交易） / 商品id（评论模块）
     */
    private Long bizId;
}
