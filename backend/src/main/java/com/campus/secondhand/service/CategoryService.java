package com.campus.secondhand.service;

import com.campus.secondhand.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /**
     * 查询所有分类（含商品数量）
     */
    List<CategoryVO> getAllCategories();

    /**
     * 根据ID查分类
     */
    CategoryVO getCategoryById(Long id);
}
