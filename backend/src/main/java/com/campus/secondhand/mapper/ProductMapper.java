package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Argentina
* @description 针对表【product(商品)】的数据库操作Mapper
* @createDate 2026-09-16 17:21:50
* @Entity com.campus.secondhand.pojo.Product
*/
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 根据关键词模糊查询商品
     * @param keyword
     * @return
     */
    List<Product> selectByTitleProduct(@Param("keyword") String keyword);

    /**
     * 分页查询商品
     * 查询前limit热度的商品，按照浏览次数降序排序
     * @param limit
     * @return
     */
    List<Product> selectHotProducts(@Param("limit") int limit);
}




