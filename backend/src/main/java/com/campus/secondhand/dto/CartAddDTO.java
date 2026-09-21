package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class CartAddDTO implements Serializable {
    @Serial
    private  static  final long serialVersionUID=1L;
    /**
     * 商品ID 强制以 productId 这个名字接收
     */
    @JsonProperty("productId")
    private Long productId;
    /**
     * 数量,可空,如果为空按照1处理
     */
    private Integer quantity;
}
