package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单明细项
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVO {

    private Long id;                // 商品id

    private Long productId;         // 对应的商品id

    private String productTitle;    // 对应的商品名

    private String productCover;    // 商品图片地址

    private BigDecimal price;       // 对应商品的购买价格

    private Integer quantity;       // 购买的数量

    private BigDecimal totalAmount; // 总金额
}
