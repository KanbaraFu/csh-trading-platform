package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.ProductQueryDTO;
import com.campus.secondhand.dto.ProductSaveDTO;
import com.campus.secondhand.mapper.ProductImageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.pojo.ProductImage;
import com.campus.secondhand.service.ProductService;
import com.campus.secondhand.vo.ProductDetailVO;
import com.campus.secondhand.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    public IPage<ProductVO> getProductPage(ProductQueryDTO queryDTO) {
        Page<ProductVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<ProductVO> result = productMapper.selectProductPage(
                page,
                queryDTO.getCategoryId(),
                queryDTO.getKeyword(),
                queryDTO.getCondition(),
                queryDTO.getMinPrice(),
                queryDTO.getMaxPrice(),
                queryDTO.getStatus(),
                queryDTO.getSellerId()
        );
        // 列表接口必须带上“是否可购买”，前端商品卡片据此判断是否置灰；
        // 该字段缺失会让前端把所有商品误判为已售出。
        result.getRecords().forEach(vo -> vo.setIsAvailable(isAvailable(vo.getStatus(), vo.getStock())));
        return result;
    }

    private boolean isAvailable(Integer status, Integer stock) {
        return status != null && status == Constants.PRODUCT_STATUS_ON
                && stock != null && stock > 0;
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        ProductDetailVO detailVO = productMapper.selectProductDetail(id);
        if (detailVO == null) {
            throw BizException.notFound("商品不存在");
        }

        // 查询图片列表
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort)
        );
        detailVO.setImages(images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList()));

        // 详情页据此决定展示「立即购买」还是「已售出」，必须显式返回
        detailVO.setIsAvailable(isAvailable(detailVO.getStatus(), detailVO.getStock()));

        return detailVO;
    }

    @Override
    @Transactional
    public Long publishProduct(Long userId, ProductSaveDTO saveDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(saveDTO, product);
        product.setSellerId(userId);
        product.setStatus(Constants.PRODUCT_STATUS_ON);
        product.setViewCount(0);
        product.setSalesCount(0);
        productMapper.insert(product);

        saveImages(product.getId(), saveDTO.getImages());
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long userId, Long productId, ProductSaveDTO saveDTO) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }
        if (!product.getSellerId().equals(userId)) {
            throw BizException.forbidden("无权修改他人商品");
        }

        BeanUtils.copyProperties(saveDTO, product);
        product.setId(productId);
        productMapper.updateById(product);

        // 先删后增图片
        productImageMapper.delete(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
        );
        saveImages(productId, saveDTO.getImages());
    }

    @Override
    public void updateStatus(Long userId, Long productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }
        if (!product.getSellerId().equals(userId)) {
            throw BizException.forbidden("无权操作他人商品");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BizException.notFound("商品不存在");
        }
        if (!product.getSellerId().equals(userId)) {
            throw BizException.forbidden("无权删除他人商品");
        }
        productMapper.deleteById(productId);
        productImageMapper.delete(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
        );
    }

    @Override
    public void incrViewCount(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setViewCount(product.getViewCount() + 1);
            productMapper.updateById(product);
        }
    }

    private void saveImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(imageUrls.get(i));
            image.setSort(i);
            productImageMapper.insert(image);
        }
    }
}
