package com.campus.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String condition;
    private Integer status;
    private Integer viewCount;
    private String cover;
    private String location;
    private LocalDateTime createTime;

    // 关联字段
    private String categoryName;
    private String sellerName;
    private String sellerAvatar;
}
