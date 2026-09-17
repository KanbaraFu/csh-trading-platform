package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.service.SearchService;
import com.campus.secondhand.vo.HotWordVO;
import com.campus.secondhand.vo.OverviewVO;
import com.campus.secondhand.vo.ProductVO;
import com.campus.secondhand.vo.SearchResultVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
            @RequestParam(required = false)String keyword,
            @RequestParam(required = false,defaultValue = "1")Long pageNum,
            @RequestParam(required = false,defaultValue = "12")Long pageSize,
            @RequestParam(required = false,defaultValue = "sort")String sort
    ){
        //根据关键词keyword查询商品信息
        SearchResultVO searchResultVO=searchService.searchByKeyword(keyword,pageNum,pageSize,sort);
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
    public Result<List<ProductVO>> getHotProducts(
            @RequestParam(required = false,defaultValue = "10") Long limit){
        List<ProductVO> hotProducts = searchService.getHotProducts(limit);
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

}
