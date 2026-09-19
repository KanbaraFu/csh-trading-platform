package com.campus.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.vo.ProductDetailVO;
import com.campus.secondhand.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

@Mapper
import java.math.BigDecimal;
import java.util.List;

/**
* @author Argentina
* @description 针对表【product(商品)】的数据库操作Mapper
* @createDate 2026-09-16 17:21:50
* @Entity com.campus.secondhand.pojo.Product
*/
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 按关键词 + 价格区间 + 排序 + 分页查在售商品
     */
    List<Product> selectByKeyword(@Param("keyword") String keyword,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("sort") String sort,
                                  @Param("offset") long offset,
                                  @Param("pageSize") long pageSize);

    /**
     * 同样条件下的总数（分页 total 用）
     */
    Long countByKeyword(@Param("keyword") String keyword,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

    IPage<ProductVO> selectProductPage(IPage<ProductVO> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword,
                                       @Param("condition") String condition,
                                       @Param("minPrice") Integer minPrice,
                                       @Param("maxPrice") Integer maxPrice,
                                       @Param("status") Integer status,
                                       @Param("sellerId") Long sellerId);
    /**
     * 分页查询商品
     * 查询前limit热度的商品，按照浏览次数降序排序
     * @param limit
     * @return
     */
    List<Product> selectHotProducts(@Param("limit") int limit);

    ProductDetailVO selectProductDetail(@Param("id") Long id);
    /**
     *
     * @param productId
     * @return
     */
    int updateRecordView(@Param("productId") Long productId);

    /**
     *
     * @param productId
     * @return
     */
    Product selectById(@Param("productId")Long productId);
}




