package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车出参,单个购物车项
 */
@Data
public class CartItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 购物车主键ID
     */
    private  Long id;
    /**
     * 归属用户id
     */
    private  Long userId;
    /**
     * 商品id
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;
    /**
     * 是否选中
     */
    private Boolean selected;
    /**
     * 创建时间
     */
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 修饰后的商品
     */
    private FACProductVO product;
    /**
     * 小计=单价*数量,保留两位小数
     */
    private BigDecimal subtotal;
}
