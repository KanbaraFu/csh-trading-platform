package com.campus.secondhand.service;

import com.campus.secondhand.vo.HotWordVO;
import com.campus.secondhand.vo.OverviewVO;
import com.campus.secondhand.vo.ProductVO;
import com.campus.secondhand.vo.SearchResultVO;

import java.util.List;

public interface SearchService {
    /**
     *
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param sort
     * @return
     */
    SearchResultVO searchByKeyword(String keyword,
                                   Long pageNum,
                                   Long pageSize,
                                   String sort);

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
    List<ProductVO> getHotProducts(Long limit);

    /**
     *
     * @return
     */
    OverviewVO getOverview();
}
