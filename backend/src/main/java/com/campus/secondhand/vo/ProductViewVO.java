package com.campus.secondhand.vo;

import com.campus.secondhand.pojo.Category;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.pojo.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

/**
 * 商品视图对象：Product 原始字段 + 装饰字段。
 *
 * <p>与前端 Mock 的 {@code decorateProduct} 输出对齐（见 docs/后端接口对接说明.md 2.1），
 * 前端列表 / 详情 / 榜单直接消费装饰后的字段，无需再做关联查询。</p>
 *
 * <p>继承 {@link Product} 是为了平铺返回全部商品字段（JSON 不嵌套）；同时继承
 * 实体里的 {@code @TableName} 等注解对 VO 无副作用（VO 不参与 CRUD）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductViewVO extends Product {

    /** 商品图片列表（按 sort 升序），为空时回退为 [cover] */
    private List<String> images;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 卖家头像 */
    private String sellerAvatar;

    /** 卖家学院 */
    private String sellerCollege;

    /** 分类名称 */
    private String categoryName;

    /** 是否可购买：status == 1 且 stock > 0 */
    private Boolean isAvailable;

    /** 榜单名次（hot-products 接口用） */
    private Long rank;

    /** 热度百分比 0-100（hot-products 接口用），序列化自动变 hot_percent */
    private Integer hotPercent;

    /**
     * 由商品实体 + 关联数据组装。关联数据批量查询后在 Service 层传入，
     * 避免逐条 N+1 查询。
     */
    public static ProductViewVO setProductViewVO(Product product, List<String> images, User seller, Category category) {
        ProductViewVO vo = new ProductViewVO();
        vo.setId(product.getId());
        vo.setSellerId(product.getSellerId());
        vo.setCategoryId(product.getCategoryId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setStock(product.getStock());
        vo.setCover(product.getCover());
        vo.setStatus(product.getStatus());
        vo.setViewCount(product.getViewCount());
        vo.setSalesCount(product.getSalesCount());
        vo.setCondition(product.getCondition());
        vo.setLocation(product.getLocation());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());

        List<String> imageList = images == null ? Collections.emptyList() : images;
        if (imageList.isEmpty() && product.getCover() != null) {
            imageList = Collections.singletonList(product.getCover());
        }
        vo.setImages(imageList);
        vo.setSellerNickname(seller != null && seller.getNickname() != null ? seller.getNickname() : "已注销用户");
        vo.setSellerAvatar(seller != null && seller.getAvatar() != null ? seller.getAvatar() : "");
        vo.setSellerCollege(seller != null && seller.getCollege() != null ? seller.getCollege() : "");
        vo.setCategoryName(category != null && category.getName() != null ? category.getName() : "未分类");
        vo.setIsAvailable(product.getStatus() != null
                && product.getStatus() == 1
                && product.getStock() != null
                && product.getStock() > 0);
        return vo;
    }
}
