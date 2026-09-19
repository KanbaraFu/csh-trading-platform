package com.campus.secondhand.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.mapper.CategoryMapper;
import com.campus.secondhand.mapper.ProductImageMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.pojo.Category;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.pojo.ProductImage;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.vo.FACProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品装饰器（组件）。
 * <p>
 * 背景：数据库 product 表只存了 sellerId、categoryId 这种“外键 id”，
 * 但前端商品卡片需要的是卖家昵称、分类名称、图片列表这些“能直接展示的内容”。
 * 把 id 翻译成展示内容的这个过程，就叫“装饰”。
 * <p>
 * 这块本来属于模块 B（商品模块）的职责，但 B 还没写，所以 C 先自己实现一份。
 * 等 B 完成后，可以直接改成调用 B 的服务，C 的业务代码不用动。
 * <p>
 * 用 @Component 交给 Spring 管理，这样 Service 里可以用构造器注入它。
 */
@Component
public class ProductDecorator {

    // 装饰需要查 4 张表，所以注入 4 个 Mapper（都是你已经建好的）
    private final ProductMapper productMapper;          // 查商品本身
    private final UserMapper userMapper;                // 查卖家信息（昵称/头像/学院）
    private final CategoryMapper categoryMapper;        // 查分类名称
    private final ProductImageMapper productImageMapper;// 查商品图片列表

    /**
     * 构造器注入：Spring 启动时会自动把这 4 个 Mapper 的实例传进来。
     * 用 final + 构造器注入是推荐写法，比 @Autowired 字段注入更安全（不可变、方便测试）。
     */
    public ProductDecorator(ProductMapper productMapper,
                            UserMapper userMapper,
                            CategoryMapper categoryMapper,
                            ProductImageMapper productImageMapper) {
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.productImageMapper = productImageMapper;
    }

    /**
     * 装饰【单个】商品。
     * 用在：收藏一个商品后，如果要立刻返回它的详情。
     *
     * @param productId 商品 id
     * @return 装饰后的 ProductVO；如果商品不存在返回 null
     */
    public FACProductVO decorate(Long productId) {
        // 防御：id 为空直接返回，避免查库报错
        if (productId == null) {
            return null;
        }
        // 先按 id 查商品，查不到说明商品被删了，返回 null
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }
        // 复用下面的批量组装逻辑（只传一个商品的列表），保证单个/批量装饰规则完全一致
        List<FACProductVO> list = assemble(Collections.singletonList(product));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 装饰【一批】商品。
     * 用在：收藏列表、购物车列表这种一次要展示多个商品的场景。
     * <p>
     * 为什么要批量？如果循环里一个个调 decorate()，10 个商品就要查 10×4=40 次数据库，
     * 这叫“N+1 查询问题”，性能很差。批量方式不管多少商品，都只查固定的 4 次。
     *
     * @param productIds 一批商品 id
     * @return 装饰后的 ProductVO 列表（调用方可自己转成 Map<id, VO> 方便取用）
     */
    public List<FACProductVO> decorateBatch(Collection<Long> productIds) {
        // 防御：入参为空直接返回空列表
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 过滤掉 null 并去重（同一个商品可能被传进来多次）
        Set<Long> ids = productIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        // 一次性把这批商品全查出来：SELECT * FROM product WHERE id IN (...)
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().in(Product::getId, ids));
        // 交给组装方法统一加工
        return assemble(products);
    }

    /**
     * 核心组装方法：把一批 Product 加工成一批 ProductVO。
     * 思路是“先批量把关联数据查成 Map，再在内存里逐个拼装”，全程只查 4 次库。
     */
    private List<FACProductVO> assemble(List<Product> products) {
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        // ---- 第 1 步：收集这批商品涉及的所有 id（商品id、卖家id、分类id）----
        Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        Set<Long> sellerIds = products.stream().map(Product::getSellerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = products.stream().map(Product::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        // ---- 第 2 步：批量查卖家，转成 Map<卖家id, 卖家对象> 方便按 id 秒查 ----
        // toMap 第三个参数 (a,b)->a 是“key 冲突时保留前者”，防止重复 key 报错
        Map<Long, User> sellerMap = sellerIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, sellerIds))
                .stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        // ---- 第 3 步：批量查分类，转成 Map<分类id, 分类对象> ----
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectList(new LambdaQueryWrapper<Category>().in(Category::getId, categoryIds))
                .stream().collect(Collectors.toMap(Category::getId, Function.identity(), (a, b) -> a));

        // ---- 第 4 步：批量查图片，按商品id分组成 Map<商品id, 图片url列表> ----
        // orderByAsc(sort) 保证图片按后台设定的顺序排列
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .in(ProductImage::getProductId, productIds)
                        .orderByAsc(ProductImage::getSort));
        Map<Long, List<String>> imageMap = images.stream().collect(Collectors.groupingBy(
                ProductImage::getProductId,                                  // 按商品id分组
                Collectors.mapping(ProductImage::getUrl, Collectors.toList())// 每组只取 url 字段，收集成 List
        ));

        // ---- 第 5 步：逐个商品拼装 ProductVO ----
        List<FACProductVO> result = new ArrayList<>(products.size());
        for (Product p : products) {
            FACProductVO vo = new FACProductVO();
            // 把 Product 里所有同名字段（id/title/price/status...）一次性拷到 vo，省去手写一堆 setter
            BeanUtils.copyProperties(p, vo);

            // 图片：优先用 product_image 表的图；没有就退化成用封面图 cover；再没有就空列表
            // （这个“回退”规则对齐前端 mock/helpers.js 的 decorateProduct）
            List<String> imgs = imageMap.getOrDefault(p.getId(), Collections.emptyList());
            if (!imgs.isEmpty()) {
                vo.setImages(imgs);
            } else if (p.getCover() != null) {
                vo.setImages(Collections.singletonList(p.getCover()));
            } else {
                vo.setImages(Collections.emptyList());
            }

            // 卖家信息：从 Map 里按 sellerId 取；卖家可能已注销（查不到），给默认值兜底
            User seller = sellerMap.get(p.getSellerId());
            vo.setSellerNickname(seller != null && seller.getNickname() != null ? seller.getNickname() : "已注销用户");
            vo.setSellerAvatar(seller != null && seller.getAvatar() != null ? seller.getAvatar() : "");
            vo.setSellerCollege(seller != null && seller.getCollege() != null ? seller.getCollege() : "");

            // 分类名称：查不到给“未分类”兜底
            Category category = categoryMap.get(p.getCategoryId());
            vo.setCategoryName(category != null && category.getName() != null ? category.getName() : "未分类");

            // 是否可购买：必须同时满足“在售(status==1)”且“有库存(stock>0)”
            // 这个字段由 C 自己算，不依赖 B。前端据此决定要不要把商品置灰
            boolean available = p.getStatus() != null
                    && p.getStatus() == Constants.PRODUCT_STATUS_ON
                    && p.getStock() != null && p.getStock() > 0;
            vo.setIsAvailable(available);

            result.add(vo);
        }
        return result;
    }
}

