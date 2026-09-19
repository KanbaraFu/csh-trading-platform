package com.campus.secondhand.service.Impl;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.mapper.*;
import com.campus.secondhand.pojo.Category;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.SearchService;
import com.campus.secondhand.utils.RedisUtil;
import com.campus.secondhand.utils.Validate;
import com.campus.secondhand.vo.*;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {
    @Resource
    private ProductMapper productMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProductImageMapper productImageMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private StatMapper statMapper;

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
    @Override
    public SearchResultVO searchByKeyword(String keyword,
                                          Long pageNum,
                                          Long pageSize,
                                          String sort,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice) {
        // 搜索词计入热词榜（Redis 不可用时忽略，不影响搜索）
        try {
            if (keyword != null && !keyword.isBlank()) {
                redisUtil.zIncrBy(Constants.REDIS_SEARCH_HOT_WORDS, keyword, 1);
            }
        } catch (Exception ignored) {
        }
        // 参数兜底
        long pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        long ps = (pageSize == null || pageSize < 1) ? 12 : pageSize;
        String st = (sort == null || sort.isBlank()) ? "new" : sort;

        // 总数（分页 total）
        Long total = productMapper.countByKeyword(keyword, minPrice, maxPrice);

        // 当前页商品（关键词 + 价格 + 排序 + 分页，一条 SQL）
        List<Product> products = productMapper.selectByKeyword(
                keyword, minPrice, maxPrice, st, (pn - 1) * ps, ps);

        // 装饰：图片/卖家/分类
        List<ProductViewVO> productViewVOList = new ArrayList<>();
        for (Product product : products) {
            List<String> images = productImageMapper.selectImageByPid(product.getId());
            User seller = userMapper.selectUserById(product.getSellerId());
            Category category = categoryMapper.selectCategoryById(product.getCategoryId());
            productViewVOList.add(ProductViewVO.setProductViewVO(product, images, seller, category));
        }

        // 4. 封装分页结果
        SearchResultVO searchResultVO = new SearchResultVO();
        searchResultVO.setKeyword(keyword);
        searchResultVO.setTotal(total);
        searchResultVO.setRecords(productViewVOList);
        searchResultVO.setPageNum(pn);
        searchResultVO.setPageSize(ps);
        return searchResultVO;
    }


    /**
     *
     * @return
     */
    @Override
    public List<HotWordVO> searchHotWord() {
        // 1. 从 Redis ZSet 取分数最高的前 12 个（倒序 = 分高在前）
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisUtil.zReverseRangeWithScores(Constants.REDIS_SEARCH_HOT_WORDS, 0, 11);

        // 2. Redis 里还没有热词 → 先灌 12 个种子词（懒初始化），再读一次
        if (tuples == null || tuples.isEmpty()) {
            initHotWords();
            tuples = redisUtil.zReverseRangeWithScores(Constants.REDIS_SEARCH_HOT_WORDS, 0, 11);
        }
        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();   // Redis 连不上时降级：空列表，前端有兜底词
        }

        // 3. 组装 VO：{word, score, rank, percent, is_hot}
        List<HotWordVO> list = new ArrayList<>();
        double max = tuples.iterator().next().getScore();   // 倒序第一个 = 最高分
        long rank = 1L;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            HotWordVO vo = new HotWordVO();
            vo.setWord(String.valueOf(tuple.getValue()));
            vo.setScore(tuple.getScore());
            vo.setRank(rank);
            vo.setPercent((int) Math.round(tuple.getScore() / max * 100));
            vo.setIsHot(rank <= 3L);
            list.add(vo);
            rank++;
        }
        return list;
    }

    /**
     *
     * @param limit
     * @return
     */
    @Override
    public List<ProductViewVO> getHotProducts(Long limit) {
        int size = (limit == null || limit <= 0) ? 10 : limit.intValue();
        List<Product> products = productMapper.selectHotProducts(size);
        // 2. 装饰：图片/卖家/分类（和 searchByKeyword 一样）
        List<ProductViewVO> list = new ArrayList<>();
        for (Product product : products) {
            //根据该商品id查询对应的商品图片
            List<String> images = productImageMapper.selectImageByPid(product.getId());
            //通过商品对应的卖家id查询卖家信息
            User seller = userMapper.selectUserById(product.getSellerId());
            //根据商品对应的分类id查询分类信息
            Category category = categoryMapper.selectCategoryById(product.getCategoryId());
            //将查到的信息封装为json数据响应给后端
            list.add(ProductViewVO.setProductViewVO(product, images, seller, category));
        }

        // 3. 算名次和热度百分比
        Integer topView = products.isEmpty() ? null : products.get(0).getViewCount();
        long maxView = (topView == null || topView <= 0) ? 1L : topView;
        for (int i = 0; i < list.size(); i++) {
            ProductViewVO vo = list.get(i);
            vo.setRank((long) (i + 1));
            vo.setHotPercent((int) Math.round(
                    (vo.getViewCount() == null ? 0 : vo.getViewCount()) * 1.0 / maxView * 100));
        }
        return list;
    }

    /**
     *
     * @return
     */
    @Override
    public OverviewVO getOverview() {
        // 一条聚合 SQL 统计 7 项（商品/在售/用户/订单/收藏/今日新增/交易额）
        OverviewVO vo = statMapper.selectOverviewStats();

        // 热词数从 Redis 取（Redis 挂了降级为 0，不报错）
        try {
            vo.setHotKeywordTotal(redisUtil.zSize(Constants.REDIS_SEARCH_HOT_WORDS));
        } catch (Exception e) {
            vo.setHotKeywordTotal(0L);
        }
        return vo;
    }

    /**
     *
     * @param productId
     * @return
     */
    @Override
    @Transactional
    public RecordViewVO getRecordView(Long productId) {
        //查询商品并验证是否存在
        Product product = productMapper.selectById(productId);
        Validate.notFound(product == null,"该商品记录不存在");
        //更新商品浏览次数+1
        int row = productMapper.updateRecordView(productId);
        Validate.verify(row != 1,"浏览记录更新失败");
        //如果能执行到这一步，说明浏览次数+1正常执行，不用再查一次，直接设置其浏览次数+1返回就行
        RecordViewVO recordViewVO=new RecordViewVO();
        recordViewVO.setProductId(productId);
        recordViewVO.setViewCount(product.getViewCount()+1);
        return recordViewVO;
    }

    /**
     *
     * @param productId
     * @return
     */
    @Override
    public SalesVO getSalesInfo(Long productId) {
        Product product = productMapper.selectById(productId);
        Validate.notFound(product == null,"该商品记录不存在");
        SalesVO salesVO=new SalesVO();
        salesVO.setProductId(productId);
        salesVO.setSalesCount(product.getSalesCount());
        salesVO.setViewCount(product.getViewCount());
        return salesVO;
    }

    /**
     * 灌入 12 个种子热词（分数 = 搜索次数）。Redis 不可用时静默降级。
     */
    private void initHotWords() {
        try {
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "考研英语真题", 186);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "iPad", 164);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "机械键盘", 142);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "自行车", 128);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "护眼台灯", 96);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "高等数学教材", 88);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "蓝牙耳机", 76);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "羽绒服", 64);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "吉他", 52);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "显示器", 45);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "羽毛球拍", 38);
            redisUtil.zAdd(Constants.REDIS_SEARCH_HOT_WORDS, "保温杯", 26);
        } catch (Exception e) {
            // Redis 不可用时忽略，热词降级为空数组
        }
    }
}
