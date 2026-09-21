package com.campus.secondhand.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单明细参数（商品ID、数量）
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    /**
     * 商品ID
     */
    @JsonProperty("productId")
    private Long productId;
    /**
     * 商品数量
     */
    @JsonProperty("quantity")
    private Integer quantity;
}
