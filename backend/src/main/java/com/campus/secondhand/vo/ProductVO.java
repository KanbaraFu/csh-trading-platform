package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表项 VO。
 * <p>sellerNickname / sellerCollege 为联查 user 表得到的装饰字段，
 * 字段名与前端 {@code utils/format.js#sellerNickname} 的兼容命名保持一致。</p>
 */
@Data
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String title;
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

    /**
     * 是否可购买：status == 1 且 stock > 0。
     * <p>显式指定 JSON 字段名：布尔类型且以 is 开头时，Jackson 会按
     * {@code isAvailable -> available} 处理，导致输出名与前端契约不一致。</p>
     */
    @JsonProperty("is_available")
    private Boolean isAvailable;
}
