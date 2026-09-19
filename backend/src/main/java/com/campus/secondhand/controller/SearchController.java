package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.service.SearchService;
import com.campus.secondhand.vo.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/api")
@RestController
public class SearchController {
    @Resource
    private SearchService searchService;
    /**
     * 根据关键词搜索商品信息
     * params{
     *     keyword:xxx
     *     pageNum:1(default)
     *     pageSize:12(default)
     *     sort:new(default)
     * }
     * @return Result(code,message)
     */
    @GetMapping("/search")
    public Result<SearchResultVO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Long pageNum,
            @RequestParam(required = false, defaultValue = "12") Long pageSize,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        SearchResultVO searchResultVO = searchService.searchByKeyword(keyword, pageNum, pageSize, sort, minPrice, maxPrice);
        return Result.success(searchResultVO);
    }

    /**
     * 获取热搜词
     * @return [{ "word": "教材",
     *          "score": 30,
     *          "rank": 1,
     *          "percent": 100,
     *          "is_hot": true
     *},{......}]热词组
     */
    @GetMapping("/search/hot-words")
    public Result<List<HotWordVO>> getHotWords(){
        List<HotWordVO> hotWordVOList = searchService.searchHotWord();
        return Result.success(hotWordVOList);
    }

    /**
     *热门榜单
     * @return
     */
    @GetMapping("/stat/hot-products")
    public Result<List<ProductViewVO>> getHotProducts(
            @RequestParam(required = false,defaultValue = "10") Long limit){
        List<ProductViewVO> hotProducts = searchService.getHotProducts(limit);
        return Result.success(hotProducts);
    }

    /**
     * 平台数据概览：6 张统计卡片
     * @return OverviewVO
     */
    @GetMapping("/stat/overview")
    public Result<OverviewVO> getOverview() {
        return Result.success(searchService.getOverview());
    }

    /**
     * 记录商品浏览量
     * @param productId
     * @return
     */
    @PostMapping("/stat/view/{productId}")
    public Result<RecordViewVO> recordView(@PathVariable Long productId){
        RecordViewVO recordViewVO = searchService.getRecordView(productId);
        return Result.success(recordViewVO);
    }

    /**
     *
     * @param productId
     * @return
     */
    @GetMapping("/stat/sales/{productId}")
    public Result<SalesVO> getSales(@PathVariable Long productId){
        SalesVO salesInfo = searchService.getSalesInfo(productId);
        return Result.success(salesInfo);
    }
}
