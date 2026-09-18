package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单
 * @TableName order
 */
@TableName(value ="order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String orderNo;

    /**
     * 
     */
    private Long buyerId;

    /**
     * 
     */
    private Long sellerId;

    /**
     * 
     */
    private BigDecimal totalAmount;

    /**
     * 
     */
    private BigDecimal payAmount;

    /**
     * 0待支付 1已支付 2已发货 3已完成 4已取消
     */
    private Integer status;

    /**
     * 下单时的收货地址快照
     */
    private String addressSnapshot;

    /**
     * 
     */
    private LocalDateTime payTime;

    /**
     * 买家下单备注
     */
    private String remark;

    /**
     * campus_card / WeChat / Alipay，模拟支付
     */
    private String payMethod;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

}