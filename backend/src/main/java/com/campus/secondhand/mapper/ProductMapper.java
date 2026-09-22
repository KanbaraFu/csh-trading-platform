package com.campus.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.vo.ProductDetailVO;
import com.campus.secondhand.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Argentina
 * @description 针对表【product(商品)】的数据库操作Mapper
 * @createDate 2026-09-16 17:21:50
 * @Entity com.campus.secondhand.pojo.Product
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 按关键词 + 分类 + 价格区间 + 排序 + 分页查在售商品
     */
    List<Product> selectByKeyword(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("sort") String sort,
                                  @Param("offset") long offset,
                                  @Param("pageSize") long pageSize);

    /**
     * 同样条件下的总数（分页 total 用）
     */
    Long countByKeyword(@Param("keyword") String keyword,
                        @Param("categoryId") Long categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 分页查询商品列表（联查分类名 + 卖家信息）
     */
    IPage<ProductVO> selectProductPage(IPage<ProductVO> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword,
                                       @Param("condition") String condition,
                                       @Param("minPrice") Integer minPrice,
                                       @Param("maxPrice") Integer maxPrice,
                                       @Param("status") Integer status,
                                       @Param("sellerId") Long sellerId,
                                       @Param("sort") String sort);

    /**
     * 查询前 limit 个热门商品，按浏览次数降序
     */
    List<Product> selectHotProducts(@Param("limit") int limit);

    /**
     * 查询商品详情（联查分类名 + 卖家信息）
     */
    ProductDetailVO selectProductDetail(@Param("id") Long id);

    /**
     * 浏览量原子自增 +1
     */
    int updateRecordView(@Param("productId") Long productId);
}
