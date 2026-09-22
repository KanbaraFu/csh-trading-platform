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

import java.util.ArrayList;
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
        return productMapper.selectProductPage(
                page,
                queryDTO.getCategoryId(),
                queryDTO.getKeyword(),
                queryDTO.getCondition(),
                queryDTO.getMinPrice(),
                queryDTO.getMaxPrice(),
                queryDTO.getStatus(),
                queryDTO.getSellerId(),
                queryDTO.getSort()
        );
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        ProductDetailVO detailVO = productMapper.selectProductDetail(id);
        if (detailVO == null) {
            throw BizException.notFound("商品不存在");
        }

        // 查询图片列表，为空时回退到封面（前端轮播依赖非空数组）
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort)
        );
        List<String> urls = images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());
        if (urls.isEmpty() && detailVO.getCover() != null) {
            urls = new ArrayList<>(List.of(detailVO.getCover()));
        }
        detailVO.setImages(urls);

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

        // 未传库存时默认 1，未传封面时用第一张图兜底
        if (product.getStock() == null) {
            product.setStock(1);
        }
        if (product.getCover() == null && saveDTO.getImages() != null && !saveDTO.getImages().isEmpty()) {
            product.setCover(saveDTO.getImages().get(0));
        }
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
        if (product.getCover() == null && saveDTO.getImages() != null && !saveDTO.getImages().isEmpty()) {
            product.setCover(saveDTO.getImages().get(0));
        }
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
        // 软下架：保留记录与图片，便于「重新上架」
        product.setStatus(Constants.PRODUCT_STATUS_OFF);
        productMapper.updateById(product);
    }

    @Override
    public void incrViewCount(Long productId) {
        // 原子自增，避免并发下计数被覆盖
        productMapper.updateRecordView(productId);
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
