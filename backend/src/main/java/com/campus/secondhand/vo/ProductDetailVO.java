package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情 VO。
 * <p>相比 {@link ProductVO} 多出 description（详情才需要）与 images（图片轮播列表）。</p>
 */
@Data
public class ProductDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String sellerNickname;
    private String sellerAvatar;
    private String sellerCollege;

    // 商品图片列表（为空时前端回退到 cover）
    private List<String> images;

    /**
     * 是否可购买：status == 1 且 stock > 0。
     * <p>显式指定 JSON 字段名，避免 Jackson 对 is 前缀布尔字段的命名处理。</p>
     */
    @JsonProperty("is_available")
    private Boolean isAvailable;
}
