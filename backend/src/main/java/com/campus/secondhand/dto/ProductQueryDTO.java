package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {

    private Long categoryId;

    private String keyword;

    private String condition;

    private Integer minPrice;

    private Integer maxPrice;

    private Integer status;

    private Long sellerId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
