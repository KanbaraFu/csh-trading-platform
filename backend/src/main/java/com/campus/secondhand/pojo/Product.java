package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private String cover;

    private Integer status;

    private Integer viewCount;

    private Integer salesCount;

    /** 成色 —— condition 是 MySQL 保留字，需用 @TableField 显式映射 */
    @TableField(value = "`condition`")
    private String condition;

    private String location;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
