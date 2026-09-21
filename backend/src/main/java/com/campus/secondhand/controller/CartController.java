package com.campus.secondhand.controller;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.CartAddDTO;
import com.campus.secondhand.dto.CartSelectAllDTO;
import com.campus.secondhand.dto.CartUpdateDTO;
import com.campus.secondhand.service.CartService;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.CartItemVO;
import com.campus.secondhand.vo.CartPreviewVO;
import com.campus.secondhand.vo.CartVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(Constants.API_PREFIX+"/cart")
public class CartController {
    private final CartService cartService;
    private final TokenUtil tokenUtil;

    public CartController(CartService cartService, TokenUtil tokenUtil) {
        this.cartService = cartService;
        this.tokenUtil = tokenUtil;
    }
    //测试,登录拦截器未使用,固定返会1L
    private Long currentUserId(String token) {
        /*
        Long userId = tokenUtil.getUserIdAndRefresh(token);
        if (userId == null) {
            throw BizException.unauthorized("登录已失效，请重新登录");
        }
        return userId;
        */
        return 1L;
    }
    /**
     * 加入购物车
     */
    @PostMapping
    public Result<CartItemVO> add(
            @RequestBody CartAddDTO dto,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
            ){
        Long userId =currentUserId(token);
        return Result.success(cartService.addToCart(userId,dto));
    }

    /**
     * 购物车列表
     * @param token
     * @return
     */
    @GetMapping
    public Result<CartVO> list(@RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
    ){
        Long userId =currentUserId(token);
        return Result.success(cartService.getCart(userId));
    }

    /**
     * 修改数量,选中状态
     * @param id 购物车主键
     * @param dto
     * @param token
     * @return
     */
    @PutMapping("/{id}")
    public Result<CartItemVO> update(
            @PathVariable Long id,
            @RequestBody CartUpdateDTO dto,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
            ){
        Long userId =currentUserId(token);
        return  Result.success(cartService.updateItem(userId,id,dto));
    }

    /**
     * 全选,取消全选
     * @param dto
     * @param token
     * @return
     */
    @PutMapping("/select-all")
    public Result<CartVO> selectAll(
            @RequestBody CartSelectAllDTO dto,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
            ){
        Long userId =currentUserId(token);
        return Result.success(cartService.selectAll(userId,dto));
    }

    /**
     * 删除购物车项
     * @param id
     * @param token
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<CartVO> remove(
            @PathVariable Long id,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
    ){
        Long userId =currentUserId(token);
        return Result.success(cartService.removeItem(userId,id));
    }

    /**
     * 结算预览,ids可选,不传则取全部已勾选的
     * @param ids
     * @param token
     * @return
     */
    @GetMapping("/preview")
    public Result<CartPreviewVO> preview(
            @RequestParam(required = false) String ids,
            @RequestHeader(value = Constants.TOKEN_HEADER,required = false) String token
    ){
        Long userId =currentUserId(token);
        return Result.success(cartService.preview(userId,parseIds(ids)));
    }

    private List<Long> parseIds(String ids) {
        List<Long> result=new ArrayList<>();
        if(StringUtils.isEmpty(ids)){
            return result;
        }
        for(String part :ids.split(",")){
            String trimmed=part.trim();
            if(StringUtils.isNotBlank(trimmed)){
                try {
                    Long v = Long.parseLong(trimmed);
                    if (v > 0) {
                        result.add(v);
                    }
                }catch (NumberFormatException ignored){
                    //忽略非法片段
                }
            }
        }
        return result;
    }


}
