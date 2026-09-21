package com.campus.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;

    // 分类下的商品数量
    private Integer productCount;
}
