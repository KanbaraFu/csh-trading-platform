package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 商品主键id，数据库自增
     */
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

    @TableField(value = "`condition`")
    private String condition;

    private String location;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
