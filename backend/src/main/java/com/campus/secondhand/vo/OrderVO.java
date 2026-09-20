package com.campus.secondhand.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单列表项（含买卖双方信息与明细）
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {

    private Long id;                    // 订单id

    private String orderNo;             // 订单号

    private Long buyerId;               // 买家id

    private Long sellerId;              // 卖家id

    private BigDecimal totalAmount;     // 需要支付的总金额

    private BigDecimal payAmount;       // 实际支付的金额

    private Integer status;             // 0待支付 1已支付 2已发货 3已完成 4已取消

    private String addressSnapshot;     // 下单时的收货地址快照

    private LocalDateTime payTime;      // 支付时间

    private String remark;              // 买家下单备注

    private String payMethod;           // 支付方式（campus_card / WeChat / Alipay，模拟支付

    private LocalDateTime createTime;   // 订单创建时间

    private LocalDateTime updateTime;   // 订单更新时间

    private String buyerNickname;       // 买家

    private String buyerAvatar;         // 买家地址

    private String sellerNickname;      // 卖家

    private String sellerAvatar;        // 卖家地址

    private List<OrderItemVO> items;      // 订单的每个商品详情

    private int itemCount;              // 该订单的商品个数

}
