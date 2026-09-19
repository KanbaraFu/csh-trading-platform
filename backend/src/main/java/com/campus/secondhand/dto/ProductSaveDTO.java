package com.campus.secondhand.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSaveDTO {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private String cover;

    /** 成色 */
    private String condition;

    private String location;

    private List<String> images;
}
