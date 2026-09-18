package com.campus.secondhand.controller;

import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.service.FavoriteService;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.FavoritePageVO;
import com.campus.secondhand.vo.FavoriteToggleVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.API_PREFIX+"/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final TokenUtil tokenUtil;

    public FavoriteController(FavoriteService favoriteService, TokenUtil tokenUtil) {
        this.favoriteService = favoriteService;
        this.tokenUtil = tokenUtil;
    }
    private Long currentUserId(String token){
/*        //接口
        Long userId = tokenUtil.getUserIdAndRefresh(token);
        if(userId==null){
            throw BizException.unauthorized("登录已失效,请重新登录");
        }
        return userId;*/
        //测试固定为用户固定为1
        return 1L;
    }

    /**
     * 收藏
     * @param productId 商品id
     * @param token token
     * @return 收藏/取消收藏接口返回视图对象
     */
    @PostMapping("/{productId}")
    public Result<FavoriteToggleVO> add(
            @PathVariable Long productId,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token){
        Long userId = currentUserId(token);
        FavoriteToggleVO vo = favoriteService.addFavorite(userId, productId);
        return Result.success(vo);

    }

    /**
     * 取消收藏
     * @param productId 商品id
     * @param token token
     * @return 收藏/取消收藏接口返回视图对象
     */
    @DeleteMapping("/{productId}")
    public Result<FavoriteToggleVO> remove(
            @PathVariable Long productId,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token){
        Long userId = currentUserId(token);
        FavoriteToggleVO favoriteToggleVO = favoriteService.removeFavorite(userId, productId);
        return Result.success(favoriteToggleVO);
    }

    /**
     * 查询我的收藏分页列表
     * @param pageNum  页码
     * @param pageSize 每页多少个
     * @param token token
     * @return  返回前端页码视图
     */
    @GetMapping
    public Result<FavoritePageVO> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "8") long pageSize,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
    ){
        Long userId = currentUserId(token);
        FavoritePageVO pageVO = favoriteService.getFavorites(userId, pageNum, pageSize);
        return Result.success(pageVO);
    }

    /**
     * 获取全部收藏列表
     * @param token token
     * @return 收藏列表
     */
    @GetMapping("/ids")
    public Result<List<Long>> ids(
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
    ){
        Long userId = currentUserId(token);
        List<Long> list = favoriteService.getFavoriteIds(userId);
        return Result.success(list);

    }
}
