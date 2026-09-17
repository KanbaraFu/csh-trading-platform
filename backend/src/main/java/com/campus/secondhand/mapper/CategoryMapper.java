package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Argentina
* @description 针对表【category(商品分类)】的数据库操作Mapper
* @createDate 2026-09-16 17:26:14
* @Entity com.campus.secondhand.pojo.Category
*/
public interface CategoryMapper extends BaseMapper<Category> {
    Category selectCategoryById(@Param("categoryId")Long categoryId);
}




