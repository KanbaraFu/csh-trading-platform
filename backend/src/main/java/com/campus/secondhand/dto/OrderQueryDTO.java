package com.campus.secondhand.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 订单列表查询参数（角色、状态、分页）
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderQueryDTO {
    private Long pageNum;
    private Long pageSize;
    private String status;
    private String role;
}
