package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Category;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.service.CategoryService;
import com.campus.secondhand.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
        );

        return categories.stream().map(cat -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(cat, vo);

            Long count = productMapper.selectCount(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getCategoryId, cat.getId())
                            .eq(Product::getStatus, Constants.PRODUCT_STATUS_ON)
            );
            vo.setProductCount(count.intValue());

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);

        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, id)
                        .eq(Product::getStatus, Constants.PRODUCT_STATUS_ON)
        );
        vo.setProductCount(count.intValue());

        return vo;
    }
}
