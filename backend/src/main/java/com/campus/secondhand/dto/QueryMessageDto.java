package com.campus.secondhand.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页，条件查询消息的参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryMessageDto {
    //消息的类型，为空查全部
    private String type;

    //页码，默认1
    private Long pageNum = 1L;

    //每页大小，默认30
    private Long pageSize = 30L;
}
