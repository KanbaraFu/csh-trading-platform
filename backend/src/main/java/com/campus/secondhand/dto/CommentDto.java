package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加评论时，接收参数所用dto对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long productId;//产品id

    private String content;//评论内容

    private Long parentId;//评论父ID
}
