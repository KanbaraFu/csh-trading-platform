package com.campus.secondhand.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite implements Serializable {
    /**
     * 序列化版本
     */
    private static final long serialVersionUID=1L;

    @TableId(type= IdType.AUTO)
    private Long id;
    //收藏者用户id
    private Long userId;
    //被收藏的商品id
    private Long productId;
    //收藏时间
    private LocalDateTime createTime;
}
