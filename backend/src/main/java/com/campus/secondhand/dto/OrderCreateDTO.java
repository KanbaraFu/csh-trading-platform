package com.campus.secondhand.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 创建订单的参数（包括明细数组、地址、备注、购物车项ID）
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateDTO {
    /**
     * 地址
     */
    @JsonProperty("addressId")
    private Long addressId;

    @JsonProperty("items")
    private List<OrderItemDTO> items;

    /**
     * 订单备注
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 下单成功要清理的购物车项ID（立即购买时为空，也就是说不加入购物车直接购买的情况下）
     */
    @JsonProperty("cartIds")
    private List<Long> cartIds;
}
