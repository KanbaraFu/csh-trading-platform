package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细
 * @TableName order_item
 */
@TableName(value ="order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long orderId;

    /**
     * 
     */
    private Long productId;

    /**
     * 下单时的商品标题快照
     */
    private String productTitle;

    /**
     * 下单时的商品封面快照
     */
    private String productCover;

    /**
     * 
     */
    private BigDecimal price;

    /**
     * 
     */
    private Integer quantity;

    /**
     * 
     */
    private BigDecimal totalAmount;

}