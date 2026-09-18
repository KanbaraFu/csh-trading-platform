package com.campus.secondhand.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.vo.OrderCountVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 分页返回结构，作为 {@link Result#getData()} 的载体。
 *
 * <pre>
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {
 *     "records": [],
 *     "total": 100,
 *     "pageNum": 1,
 *     "pageSize": 10
 *   }
 * }
 * </pre>
 *
 * <p>典型用法：{@code PageResult.of(productService.page(new Page<>(pageNum, pageSize), wrapper))}。</p>
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /**
     * 当前页码，从 1 开始。
     *
     * <p>显式声明 JSON 名字：全局命名策略是 SNAKE_CASE（见 application.yml），
     * 不加注解会被序列化成 {@code page_num}，破坏前端分页契约（文档 1.4 要求 camelCase）。</p>
     */
    @JsonProperty("pageNum")
    private Long pageNum;

    /** 每页条数。原因同 {@link #pageNum}，前端契约固定为 {@code pageSize} */
    @JsonProperty("pageSize")
    private Long pageSize;

    /** 订单页面当前状态，all:全部、0:待付款、1:待发货、2:待收货、3:已完成、4:已取消 */
    private OrderCountVO orderCounts;

    public PageResult(List<T> records, Long total, Long pageNum, Long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 直接由 MyBatis-Plus 的 {@link IPage} 转换。
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return empty(Constants.DEFAULT_PAGE_NUM, Constants.DEFAULT_PAGE_SIZE);
        }
        List<T> records = page.getRecords() == null ? Collections.emptyList() : page.getRecords();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 由 {@link IPage} 的分页信息 + 转换后的列表组装。
     * 适用于 Pojo 分页后需要转成 VO 的场景。
     */
    public static <T, S> PageResult<T> of(IPage<S> page, List<T> records) {
        if (page == null) {
            return empty(Constants.DEFAULT_PAGE_NUM, Constants.DEFAULT_PAGE_SIZE);
        }
        return new PageResult<>(
                records == null ? Collections.emptyList() : records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize());
    }

    /**
     * 方法同上方，但这个适用于订单页面当前选择的状态
     */
    public static <T, S> PageResult<T> of(IPage<S> page, List<T> records, OrderCountVO orderCounts) {
        if (page == null) {
            return empty(Constants.DEFAULT_PAGE_NUM, Constants.DEFAULT_PAGE_SIZE);
        }
        return new PageResult<>(
                records == null ? Collections.emptyList() : records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                orderCounts);
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        return new PageResult<>(
                records == null ? Collections.emptyList() : records,
                total,
                pageNum,
                pageSize);
    }

    /**
     * 空分页结果，查询无数据时可复用，避免前端拿到 null 还要判空。
     */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0L, pageNum, pageSize);
    }

    /**
     * 总页数（向上取整），前端分页组件有时需要。
     */
    public long getPages() {
        if (total == null || pageSize == null || pageSize <= 0L) {
            return 0L;
        }
        return (total + pageSize - 1) / pageSize;
    }
}
