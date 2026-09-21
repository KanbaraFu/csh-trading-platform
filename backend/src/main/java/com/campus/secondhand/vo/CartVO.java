package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车汇总出参(非分页)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartVO implements Serializable {
    @Serial
    private static  final long serialVersionUID=1L;


    /**
     * 购物车项列表
     */
    private List<CartItemVO> records;
    /**
     * 全部商品总数
     */
    private Integer totalQuantity;
    /**
     * 已经勾选商品总数
     */
    private Integer selectedQuantity;
    /**
     * 已经勾选的商品总额
     */
    private  BigDecimal selectedAmount;
    /**
     * 是否全选
     */
    private Boolean AllSelected;

}
