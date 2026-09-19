package com.campus.secondhand.service;

import com.campus.secondhand.vo.*;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {
    /**
     *
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param sort
     * @param minPrice
     * @param maxPrice
     * @return
     */
    SearchResultVO searchByKeyword(String keyword,
                                   Long pageNum,
                                   Long pageSize,
                                   String sort,
                                   BigDecimal minPrice,
                                   BigDecimal maxPrice);


    /**
     *
     * @return
     */
    List<HotWordVO> searchHotWord();

    /**
     *
     * @param limit
     * @return
     */
    List<ProductViewVO> getHotProducts(Long limit);

    /**
     *
     * @return
     */
    OverviewVO getOverview();

    /**
     *
     * @param productId
     * @return
     */
    RecordViewVO getRecordView(Long productId);

    /**
     *
     * @param productId
     * @return
     */
    SalesVO getSalesInfo(Long productId);
}
