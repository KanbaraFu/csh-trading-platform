package com.campus.secondhand.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class CartSelectAllDTO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    /**
     * true=全选中
     */
    private Boolean selected;
}
