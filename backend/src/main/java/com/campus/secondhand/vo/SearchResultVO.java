package com.campus.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索接口返回结构：{@code keyword} + 标准分页结构。
 *
 * <p>与前端契约一致：{@code pageNum / pageSize} 必须输出 camelCase
 * （全局命名策略是 SNAKE_CASE，需要显式 {@link JsonProperty} 声明，
 * 参见 {@code PageResult} 的处理方式）。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultVO {

    /** 搜索关键词（原样回显） */
    private String keyword;

    /** 当前页商品（装饰后） */
    private List<ProductVO> records;

    /** 总条数 */
    private Long total;

    /** 当前页码 */
    @JsonProperty("pageNum")
    private Long pageNum;

    /** 每页条数 */
    @JsonProperty("pageSize")
    private Long pageSize;
}
