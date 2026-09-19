package com.campus.secondhand.vo;

import com.campus.secondhand.pojo.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 装饰后商品出参。
 * 注意：images / seller_* / category_name 属模块 B 装饰字段，C-1 阶段先写死占位，
 * 后续接入模块 B 时替换填充逻辑即可（见 FavoriteServiceImpl.toProductVO）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FACProductVO extends Product {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 商品图片列表
     */
    private List<String> images;

    /**
     * 卖家昵称
     */
    private String sellerNickname;

    /**
     * 卖家头像地址
     */
    private String sellerAvatar;

    /**
     * 卖家学院
     */
    private String sellerCollege;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 是否可购买：status==1 && stock>0，由 C 自己算，不依赖 B
     * 返回前端JSON字段名：is_available
     */
    @JsonProperty("is_available")
    private Boolean isAvailable;
}
