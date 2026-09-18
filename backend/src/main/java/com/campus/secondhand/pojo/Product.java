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

/**
 * 商品实体，映射 product 表。
 */
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

    /**
     * 卖家用户id
     */
    private Long sellerId;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述详情
     */
    private String description;

    /**
     * 售卖价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品封面图片地址
     */
    private String cover;

    /**
     * 商品状态：0 下架 / 1 在售 / 2 已售
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 成交销量
     */
    private Integer salesCount;

    /**
     * 商品成色；condition 是 MySQL 保留字，必须反引号转义
     */
    @TableField(value = "`condition`")
    private String condition;

    /**
     * 商品所在地点
     */
    private String location;

    /**
     * 创建时间，插入时自动填充
     * 时间格式：MM-dd HH:mm（项目未配全局日期格式，逐字段标注）
     */
    //@TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间，插入和更新时自动填充
     * 时间格式：MM-dd HH:mm（项目未配全局日期格式，逐字段标注）
     */
    //@TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
