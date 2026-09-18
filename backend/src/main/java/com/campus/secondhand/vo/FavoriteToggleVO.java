package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 收藏/取消收藏接口返回视图对象
 * 用于点击收藏按钮操作之后，返回给前端的结果
 * 返回字段：商品id、当前是否已收藏
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteToggleVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 被操作的商品id
     */
    private Long productId;

    /**
     * 收藏状态 true=已收藏，false=未收藏
     */
    private Boolean favorited;
}
