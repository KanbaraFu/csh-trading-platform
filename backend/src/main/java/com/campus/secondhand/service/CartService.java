package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.dto.CartAddDTO;
import com.campus.secondhand.dto.CartSelectAllDTO;
import com.campus.secondhand.dto.CartUpdateDTO;
import com.campus.secondhand.pojo.Cart;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.vo.CartItemVO;
import com.campus.secondhand.vo.CartPreviewVO;
import com.campus.secondhand.vo.CartVO;

import java.util.List;

public interface CartService extends IRepository<Cart> {

    /**
     * 加入购物车
     * 规则：不能加自己发布的商品；已下架/已售不能加；数量缺省 1 且不超库存；
     * 已存在同一商品则累加数量并置为选中。写入后同步 Redis Hash。
     */
    CartItemVO addToCart(Long userId, CartAddDTO dto);
    /**
     * 购物车列表
     */
    CartVO getCart(Long userId);

    /**
     * 修改购物车项,数量/选中
     */
    CartItemVO updateItem(Long userId, Long id, CartUpdateDTO dto);

    /**
     * 全选,取消全选
     */
    CartVO selectAll(Long userId, CartSelectAllDTO dto);
    /**
     * 删除购物车,仅删除自己选中的项
     */
    CartVO removeItem(Long userId,Long id);
    /**
     * 结算预览。cartIds 为空时默认取全部已勾选项。
     * 无选中项、或含不可购买商品时抛业务异常。
     */
    CartPreviewVO preview(Long userId, List<Long> cartIds);

    /**
     * 让订单模块调用,下单后按照购物车ID批量清理,并同步删除Redis缓存
     *
     */

    void clearCartItems(Long userId,List<Long> cartIds);
}
