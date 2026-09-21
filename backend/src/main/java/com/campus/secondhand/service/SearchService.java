package com.campus.secondhand.service;

import com.campus.secondhand.dto.SearchDTO;
import com.campus.secondhand.vo.*;

import java.util.List;

public interface SearchService {
    /**
     * 按关键词 + 分类 + 价格 + 排序 + 分页搜索商品
     * @param searchDTO 搜索参数（keyword/categoryId/pageNum/pageSize/sort/minPrice/maxPrice）
     * @return 分页结果
     */
    SearchResultVO searchByKeyword(SearchDTO searchDTO);


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
