package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算预览
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartPreviewVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参与结算的明细
     */
    private List<CartItemVO> items;
    /**
     * 结算总件数
     */
    private  Integer totalQuantity;
    /**
     * 商品总额
     */
    private BigDecimal totalAmount;
    /**
     * 优惠额度,统一为0
     */
    private BigDecimal discountAmount;
    /**
     * 付费金额=总额-优惠
     */
    private  BigDecimal payAmount;
    /**
     * 设计卖家数量
     */
    private  Integer SellerCount;
    /**
     * 是否为单一卖家
     */
    private Boolean SingleSeller;
    /**
     * 参与结算时购物车ID列表,为清除订单模块用于清理
     */
    private List<Long> cartIds;
}
