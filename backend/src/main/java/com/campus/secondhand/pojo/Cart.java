package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 商品id
     */
    private Long productId;
    /**
     * 数量默认1
     */
    private Integer quantity;
    /**
     * 是否选中 购物车里面的勾选
     */
    private Integer selected;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
