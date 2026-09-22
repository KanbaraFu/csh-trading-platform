package com.campus.secondhand.dto;

import lombok.Data;

/**
 * 商品列表查询参数
 */
@Data
public class ProductQueryDTO {

    private Long categoryId;

    private String keyword;

    private String condition;

    private Integer minPrice;

    private Integer maxPrice;

    /**
     * 商品状态：不传时默认只查在售(1)，避免已下架商品泄漏到首页；
     * 需要查全部（如卖家中心）时可显式传入。
     */
    private Integer status;

    private Long sellerId;

    /**
     * 排序：hot 热度 / price_asc 价格升 / price_desc 价格降 / 其他值按最新
     */
    private String sort;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
