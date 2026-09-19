package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.ProductImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Argentina
* @description 针对表【product_image(商品图片)】的数据库操作Mapper
* @createDate 2026-09-16 17:48:44
* @Entity com.campus.secondhand.pojo.ProductImage
*/
public interface ProductImageMapper extends BaseMapper<ProductImage> {
    List<String> selectImageByPid(@Param("productId") Long productId);

}




