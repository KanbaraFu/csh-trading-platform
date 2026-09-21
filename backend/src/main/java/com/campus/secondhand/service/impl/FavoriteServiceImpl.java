package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.mapper.FavoriteMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Favorite;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.service.FavoriteService;
import com.campus.secondhand.support.ProductDecorator;
import com.campus.secondhand.vo.FavoritePageVO;
import com.campus.secondhand.vo.FavoriteToggleVO;
import com.campus.secondhand.vo.FavoriteVO;
import com.campus.secondhand.vo.FACProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收藏业务实现
 */
@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    /**
     * 收藏页默认8条
     */
    private static final long DEFAULT_FAVORITE_PAGE_SIZE=8L;

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;  //检验商品是否存在
    private final ProductDecorator productDecorator;//把商品修饰到前端
    //构造器注入
    public FavoriteServiceImpl(FavoriteMapper favoriteMapper,
                               ProductMapper productMapper,
                               ProductDecorator productDecorator){
        this.favoriteMapper=favoriteMapper;
        this.productMapper=productMapper;
        this.productDecorator=productDecorator;
    }

    /**
     * 收藏商品
     * @param userId 用户ID
     * @param productId 要收藏的商品ID
     * @return
     */
    @Override
    public FavoriteToggleVO addFavorite(Long userId, Long productId) {
        //1.确定商品是否存在
        Product product = productMapper.selectById(productId);
        if(product ==null){
            throw BizException.notFound("商品不存在");
        }
        //2.查询用户是否已经收藏了这个商品
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));

        //3没收藏过,则插入,已经收藏了,就什么都不做,直接返回成功
        if(count==null||count==0){
            Favorite favorite =new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favorite.setCreateTime(LocalDateTime.now());
            try{
                favoriteMapper.insert(favorite);
            }catch (DuplicateKeyException e){
                log.info("用户重复收藏,productId :{}",productId);
            }

        }
        //4.无论新增还是本来就收藏咧.都返回true
        return new FavoriteToggleVO(productId,true);

    }


    /**
     * 取消收藏
     * @param userId
     * @param productId
     * @return
     */
    @Override
    public FavoriteToggleVO removeFavorite(Long userId, Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId,userId)
                .eq(Favorite::getProductId,productId)
        );
        //false 取消收藏逻辑,返回给前端
        return new FavoriteToggleVO(productId,false);
    }

    /**
     * 我的收藏列表,分页,全量总价,下架商品返回
     * @param userId
     * @param pageNum 第几页(从1开始)
     * @param pageSize 每页几个
     * @return
     */
    @Override
    public FavoritePageVO getFavorites(Long userId, long pageNum, long pageSize) {
        //1.参数兜底
        long num =pageNum<=0? Constants.DEFAULT_PAGE_NUM:pageNum;
        long size =pageSize<=0 ? DEFAULT_FAVORITE_PAGE_SIZE:pageSize;
        //2.查询该用户所有收藏,倒叙排列
        List<Favorite> all = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        //没有收藏返回空页,总价值为0
        if(all.isEmpty()){
            return new FavoritePageVO(Collections.emptyList(),0L,num,size, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        //3.批量查询商品解决n+1问题
        List<Long> productIds = all.stream().map(Favorite::getProductId).distinct().toList();
        Map<Long, FACProductVO> productMap = productDecorator.decorateBatch(productIds).stream().collect(Collectors.toMap(FACProductVO::getId, Function.identity(), (a, b) -> a));
        //4.组装收藏vo集合+计算总价(分页前)
        List<FavoriteVO> full =new ArrayList<>();
        BigDecimal totalAmount=BigDecimal.ZERO;
        for(Favorite f:all){
            FACProductVO product = productMap.get(f.getProductId());
            if(product==null){
                continue;
            }
            full.add(new FavoriteVO(f.getId(),f.getProductId(),product));
            if(product.getPrice()!=null){
                totalAmount=totalAmount.add(product.getPrice());
            }
        }
        totalAmount=totalAmount.setScale(2,RoundingMode.UP);
        //5.内存分页
        long total =full.size();
        int from =(int)((num-1)*size); //size 每页几个
        List<FavoriteVO> records = from >= total ? Collections.emptyList() : full.subList(from, (int) Math.min(from + size, total));

        return new FavoritePageVO(records,total,num,size,totalAmount);
    }

    /**
     *
     * 得到所有收藏ID
     * @param userId
     * @return
     */
    @Override
    public List<Long> getFavoriteIds(Long userId) {
        return favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .select(Favorite::getProductId)
                .eq(Favorite::getUserId,userId)
                .orderByDesc(Favorite::getCreateTime))
                .stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
    }
}
