package com.campus.secondhand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询：分页查询评论接收的参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentPageDto {
    private Long productId;

    private Long pageNum=1L;//页码

    private Long pageSize=10L;//每页大小
}
