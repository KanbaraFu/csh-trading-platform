package com.campus.secondhand.vo;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author KanbaraFu
 * @version 1.0
 * @description 各状态订单数量，all:全部、0:待付款、1:待发货、2:待收货、3:已完成、4:已取消
 * @since 2026-09-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCountVO {

    @JSONField(name = "counts")
    private Map<String, Long> orderCounts;
}
