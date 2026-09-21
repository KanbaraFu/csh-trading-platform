package com.campus.secondhand.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 创建订单时用来存放的商品信息
 * @since 2026-09-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVO {

    private Long productId;

    private Long sellerId;

    private String productTitle;

    private String productCover;

    private Integer productStock;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private Integer quantity;

}
