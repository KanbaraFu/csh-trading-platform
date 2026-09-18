package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.ProductQueryDTO;
import com.campus.secondhand.dto.ProductSaveDTO;
import com.campus.secondhand.vo.ProductDetailVO;
import com.campus.secondhand.vo.ProductVO;

public interface ProductService {

    /**
     * 分页查询商品列表
     */
    IPage<ProductVO> getProductPage(ProductQueryDTO queryDTO);

    /**
     * 获取商品详情
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 发布商品
     */
    Long publishProduct(Long userId, ProductSaveDTO saveDTO);

    /**
     * 更新商品
     */
    void updateProduct(Long userId, Long productId, ProductSaveDTO saveDTO);

    /**
     * 上架/下架商品
     */
    void updateStatus(Long userId, Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long userId, Long productId);

    /**
     * 增加浏览量
     */
    void incrViewCount(Long productId);
}
