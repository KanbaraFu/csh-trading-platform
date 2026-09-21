package com.campus.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String condition;
    private Integer status;
    private Integer viewCount;
    private Integer salesCount;
    private String cover;
    private String location;
    private LocalDateTime createTime;

    // 关联字段
    private String categoryName;
    private String sellerName;
    private String sellerAvatar;

    // 商品图片列表
    private List<String> images;

    /**
     * 是否可购买：status == 1 且 stock > 0。
     */
    private Boolean isAvailable;
}
