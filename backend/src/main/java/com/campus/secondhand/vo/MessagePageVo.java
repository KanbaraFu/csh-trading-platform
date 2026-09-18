package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 前端数据需要count加消息信息，
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageVo {

    /** 当前页消息列表 */
    private List<MessageVo> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    /** 各分类计数 */
    private Map<String, Long> counts;

    public MessagePageVo(List<MessageVo> records, Long total, Long pageNum, Long pageSize, Long all, Long unread, Long system, Long trade, Long comment) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.counts = Map.of(
                "all", all,
                "unread", unread,
                "system", system,
                "trade", trade,
                "comment", comment
        );
    }
}
