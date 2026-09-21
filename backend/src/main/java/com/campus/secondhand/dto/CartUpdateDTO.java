package com.campus.secondhand.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class CartUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    /**
     * 目标数量,最小为1,最大不超过库存
     */
    private Integer quantity;
    /**
     * 目标选中状态
     */
    private Boolean selected;

}
