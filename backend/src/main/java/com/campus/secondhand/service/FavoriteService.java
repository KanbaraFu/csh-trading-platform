package com.campus.secondhand.service;

import com.campus.secondhand.vo.FavoritePageVO;
import com.campus.secondhand.vo.FavoriteToggleVO;

import java.util.List;

/**
 * 收藏业务接口
 */
public interface FavoriteService {


    /**
     * 收藏商品
     * @param userId 用户ID
     * @param productId 要收藏的商品ID
     * @return  {product_id  favorited=true}
     */
    FavoriteToggleVO addFavorite(Long userId,Long productId);

    /**
     * 取消收藏
     * @param userId
     * @param productId
     * @return
     */

    FavoriteToggleVO removeFavorite(Long userId ,Long productId);

    /**
     *  收藏列表分页返回,并带上全部收藏商品总价
     * @param userId
     * @param pageNum 第几页(从1开始)
     * @param pageSize 每页几页
     * @return
     */
    FavoritePageVO getFavorites(Long userId,long pageNum,long pageSize);

    /**
     * 当前用户收藏的所有商品ID列表
     * 前端拿到这个列表可以一次性把哪些已经收藏的进行点亮
     * @param userId
     * @return
     */

    List<Long> getFavoriteIds(Long userId);








}

