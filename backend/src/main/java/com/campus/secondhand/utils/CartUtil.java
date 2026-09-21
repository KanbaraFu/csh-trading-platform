package com.campus.secondhand.utils;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.pojo.Cart;
import com.campus.secondhand.support.ProductDecorator;
import com.campus.secondhand.vo.CartItemVO;
import com.campus.secondhand.vo.CartVO;
import com.campus.secondhand.vo.FACProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Component
public class CartUtil {
    private  final RedisUtil redisUtil;
    private final ProductDecorator productDecorator;

    public CartUtil(RedisUtil redisUtil, ProductDecorator productDecorator) {
        this.redisUtil = redisUtil;
        this.productDecorator = productDecorator;
    }

    /**
     * Redis Hash 缓存同步工具方法
     * @param cart 购物车实体
     */
    public void syncCache(Cart cart){
        String key = Constants.cartKey(cart.getUserId());
        redisUtil.hSet(key,String.valueOf(cart.getProductId()),cart);
        redisUtil.expire(key,Constants.CART_TTL);
    }

    /**
     * 实体转视图对象
     * @param cart  购物车实体
     * @param FACProductVO 商品视图
     * @return 前端需要的视图
     */
    public CartItemVO toItemVO(Cart cart, FACProductVO FACProductVO){
        CartItemVO cartItemVO=new CartItemVO();
        cartItemVO.setId(cart.getId());
        cartItemVO.setUserId(cart.getUserId());
        cartItemVO.setProductId(cart.getProductId());
        cartItemVO.setQuantity(cart.getQuantity());
        //数据库库存0/1前度需要布尔值
        cartItemVO.setSelected(cart.getSelected() != null && cart.getSelected() == Constants.FLAG_YES);
        cartItemVO.setCreatTime(cart.getCreateTime());
        cartItemVO.setUpdateTime(cart.getUpdateTime());
        cartItemVO.setProduct(FACProductVO);
        cartItemVO.setSubtotal(calcSubtotal(FACProductVO,cart.getQuantity()));//总价
        return cartItemVO;
    }

    /**
     * 商品价格,单价*数量
     * @param FACProductVO 商品vo
     * @param quantity 数量
     * @return 返回的金额,保留两位小数,四舍五入
     */
    public BigDecimal calcSubtotal(FACProductVO FACProductVO, Integer quantity) {
        BigDecimal price = (FACProductVO != null && FACProductVO.getPrice() != null) ? FACProductVO.getPrice() : BigDecimal.ZERO;
        int qty =quantity==null?0:quantity;
        return price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 将carts实体转化为CartVO
     * @param carts 当前购物车的实体集合
     * @return cartVO
     */
    public CartVO buildCartVO(List<Cart> carts){
        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if(carts==null||carts.isEmpty()){
            return new CartVO(Collections.emptyList(), 0, 0, zero, false);
        }
        //获取所有商品id,批量查询商品信息
        List<Long> products = carts.stream().map(Cart::getProductId).distinct().toList();
        Map<Long, FACProductVO> productVOMap = productDecorator.decorateBatch(products).stream().collect(Collectors.toMap(productVO -> productVO.getId(), Function.identity(), (a, b) -> a));

        List<CartItemVO> records =new ArrayList<>();
        int totalQty=0;//购物车商品总数
        int selectedQty =0;//选中商品总数
        BigDecimal selectedAmount =BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP);//选中商品总额
        boolean allSelected =true; //是否全选
        for(Cart c: carts){
            FACProductVO p = productVOMap.get(c.getProductId());
            if(p==null){
                log.warn("购物车商品不存在,已跳过.cartId={},productId={}",c.getId(),c.getProductId());
                allSelected=false;
                continue;
            }
            CartItemVO cartItemVO=toItemVO(c,p);
            records.add(cartItemVO);
            totalQty+=c.getQuantity();
            //判断是否选中,如果选中的数量和金额
            if(Boolean.TRUE.equals(cartItemVO.getSelected())){
                selectedQty+=c.getQuantity();
                selectedAmount=selectedAmount.add(cartItemVO.getSubtotal());
            }else{
                allSelected=false;
            }
        }
        //空列表强制取消全选；有列表，维持原有全选标记
        allSelected=!records.isEmpty()&&allSelected;
        return new CartVO(records,totalQty,selectedQty,selectedAmount,allSelected);
    }

    /**
     * 重建购物车缓存,先删除整个hash,循环写入购物项,全选场景使用
     * @param userId 用户ID
     * @param carts 购物车实例集合
     */
    public void rebuildCache(Long userId,List<Cart> carts){
        String key = Constants.cartKey(userId);
        redisUtil.delete(key);
        if(carts==null||carts.isEmpty()){
            return;
        }
        for(Cart c :carts){
            redisUtil.hSet(key,String.valueOf(c.getProductId()),c);
        }
        redisUtil.expire(key,Constants.CART_TTL);
    }

    /**
     * 删除Hsah里面的某个商品field
     * @param userId 用户id
     * @param productId 商品id
     */
    public void removeCacheField(Long userId,Long productId){
        redisUtil.hDel(Constants.cartKey(userId),String.valueOf(productId));

    }
}
