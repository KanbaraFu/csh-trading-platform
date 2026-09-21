package com.campus.secondhand.vo;

import com.campus.secondhand.common.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收藏分页视图对象：封装收藏分页列表数据 + 全部收藏商品总价
 * 继承通用分页返回对象 PageResult，在原有分页基础上额外增加总价统计字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoritePageVO extends PageResult<FavoriteVO> {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     *      * 全部收藏商品的总价（分页前统计，不受当前页影响，翻页保持不变）
     */
    private BigDecimal totalAmount;

    /**
     * 全参构造方法，用于组装分页记录、分页参数与总价
     * @param records 当前页收藏VO列表
     * @param total 收藏记录总条数
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param totalAmount 当前页商品合计总价
     */
    public FavoritePageVO(List<FavoriteVO> records, long total, long pageNum, long pageSize, BigDecimal totalAmount) {
        super(records, total, pageNum, pageSize);
        this.totalAmount = totalAmount;
    }
}
