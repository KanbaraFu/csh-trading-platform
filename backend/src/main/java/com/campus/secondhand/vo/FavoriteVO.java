package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 收藏项视图对象，用于收藏列表接口返回前端。
 * 包含收藏记录id、商品id，以及完整的商品VO信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 收藏记录主键id
     */
    private Long id;

    /**
     * 被收藏的商品id
     */
    private Long productId;

    /**
     * 商品详情VO，封装商品完整展示信息
     */
    private FACProductVO product;
}
