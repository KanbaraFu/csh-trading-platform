package com.campus.secondhand.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchDTO {
    private String keyword;
    private Long categoryId;
    private Long pageNum = 1L;
    private Long pageSize = 12L;
    private String sort = "new";
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
