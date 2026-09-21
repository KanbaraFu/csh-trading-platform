package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.CartAddDTO;
import com.campus.secondhand.dto.CartSelectAllDTO;
import com.campus.secondhand.dto.CartUpdateDTO;
import com.campus.secondhand.mapper.CartMapper;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.Cart;
import com.campus.secondhand.pojo.Product;
import com.campus.secondhand.service.CartService;
import com.campus.secondhand.support.ProductDecorator;
import com.campus.secondhand.utils.CartUtil;
import com.campus.secondhand.vo.CartItemVO;
import com.campus.secondhand.vo.CartPreviewVO;
import com.campus.secondhand.vo.CartVO;
import com.campus.secondhand.vo.FACProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车业务实现
 */
@Slf4j
@Service
public class CartServiceImpl extends CrudRepository<CartMapper, Cart> implements CartService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper; //检验商品是否存在
    private final ProductDecorator productDecorator;//装饰器,将product包装成前端需要的productVO
    private final CartUtil cartUtil; //redis工具类,操作redis缓存

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper, ProductDecorator productDecorator, CartUtil cartUtil) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.productDecorator = productDecorator;
        this.cartUtil = cartUtil;
    }

    /**
     * 添加商品到购物车
     * @param userId 用户id
     * @param dto 前端传入的参数,productId商品Id和quantity商品数量
     * @return 购物车项的完整信息
     */

    @Override
    public CartItemVO addToCart(Long userId, CartAddDTO dto) {
        //dto为空,抛出异常
        if(dto==null||dto.getProductId()==null){
            throw BizException.badRequest("商品ID不能为空");
        }
        Long productId=dto.getProductId();
        //判断商品是否存在
        Product product = productMapper.selectById(productId);
        if(product==null){
            throw BizException.notFound("商品不存在");
        }
        //不能买自己的商品
        if(userId.equals(product.getSellerId())){
            throw BizException.badRequest("不能购买自己发布的商品");
        }
        //检验商品,库存必须大于零
        if (product.getStatus() == null
                || product.getStatus() != Constants.PRODUCT_STATUS_ON
                || product.getStock() == null || product.getStock() <= 0
        ) {
            throw BizException.badRequest("商品已出售或已下架");
        }
        //前端不传数量,或数量传递小于1,设置默认值为1
        int addQty =(dto.getQuantity()==null||dto.getQuantity()<1?1:dto.getQuantity());
        int stock =product.getStock(); //库存数量
        //查询当前用户购物车里有没有该商品
        LambdaQueryWrapper<Cart> queryWrapper=new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId,userId)
                .eq(Cart::getProductId,productId);
        Cart exist =cartMapper.selectOne(queryWrapper);

        LocalDateTime now =LocalDateTime.now();
        Cart saved; //购物车里的对象

        if(exist!=null){
            //购物车已经有对象了,只需要累加数量
            int nextQty =Math.min(exist.getQuantity()+addQty,stock );
            exist.setQuantity(nextQty);
            exist.setSelected(Constants.FLAG_YES);//默认选中
            exist.setUpdateTime(now);
            cartMapper.updateById(exist);
            saved=exist;
        }else{
            //购物车没有商品,新建购物车记录
            Cart cart =new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(Math.min(addQty,stock));
            cart.setSelected(Constants.FLAG_YES);
            cart.setCreateTime(now);
            cart.setUpdateTime(now);
            cartMapper.insert(cart);
            saved=cart;
        }
        //更新Redis镜像缓存
        cartUtil.syncCache(saved);
        //调用装饰器获取商品vo,组装CartItemVO返回前端
        FACProductVO FACProductVO = productDecorator.decorate(productId);

        return cartUtil.toItemVO(saved, FACProductVO);
    }

    /**
     * 查询购物车列表
     * @param userId 用户id
     * @return
     */
    @Override
    public CartVO getCart(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
                .orderByDesc(Cart::getId)
        );
        return cartUtil.buildCartVO(carts);
    }

    /**
     * 修改购物车单条记录
     * @param userId 用户ID
     * @param id 购物车记录主键id
     * @param dto 修改的参数(数量/是否选中)
     * @return 修改后的购物车单项
     */
    @Override
    @Transactional(rollbackFor = Exception.class)//事物
    public CartItemVO updateItem(Long userId, Long id, CartUpdateDTO dto) {
        //拿到购物车单项
        Cart cart = cartMapper.selectById(id);
        if(cart==null){
            throw BizException.notFound("购物车项目不存在");
        }
        //权限校验只能修改自己的购物车
        if(!userId.equals(cart.getUserId())){
            throw BizException.forbidden("无权操作他人的购物车");
        }
        if(dto==null){
            throw BizException.notFound("没有发现要修改的内容");
        }
        //如果前端传入了参数,校验数量合法性
        if(dto.getQuantity()!=null) {
            Integer qty = dto.getQuantity();
            if (qty < 1) {
                throw BizException.badRequest("数量不能小于1");
            }
            //检查不能超过库存
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null && product.getStock() != null && qty > product.getStock()) {
                throw BizException.badRequest("商品仅剩余" + product.getStock() + "件");
            }
            cart.setQuantity(qty);
        }
            //如果传了选中状态,布尔值转化为数据库存储的01常量
            if(dto.getSelected()!=null){
                cart.setSelected(Boolean.TRUE.equals(dto.getSelected())?Constants.FLAG_YES:Constants.FLAG_NO);
            }
            //更新修改时间
            cart.setUpdateTime(LocalDateTime.now());
            //写入数据库
            cartMapper.updateById(cart);
            //同步更新Redis缓存
            cartUtil.syncCache(cart);
            //组装vo返回
            FACProductVO fACProductVO = productDecorator.decorate(cart.getProductId());
            return cartUtil.toItemVO(cart,fACProductVO);


    }

    /**
     * 全选/取消全选
     * @param userId 用户id
     * @param dto  选中标记,true为全选
     * @return carevo 购物车汇总
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO selectAll(Long userId, CartSelectAllDTO dto) {
        //判断目标状态
        int target  =(dto!=null&&Boolean.TRUE.equals(dto.getSelected()))?Constants.FLAG_YES:Constants.FLAG_NO;
        //更新
        cartMapper.update(null,new LambdaUpdateWrapper<Cart>()
                .set(Cart::getSelected,target)
                .set(Cart::getUpdateTime,LocalDateTime.now())
                        .eq(Cart::getUserId,userId)
                );
        //重新查询
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
                .orderByDesc(Cart::getId));
        //重建Redis缓存
        cartUtil.rebuildCache(userId,carts);

        return cartUtil.buildCartVO(carts);
    }

    /**
     * 删除一条购物车记录
     * @param userId 用户ID
     * @param id 购物车主键ID
     * @return 删除之后的最新购物车汇总
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO removeItem(Long userId, Long id) {
        Cart cart = cartMapper.selectById(id);
        if(cart==null){
            throw BizException.notFound("购物车项不存在");
        }
        //权限校验
        if(!userId.equals(cart.getUserId())){
            throw BizException.forbidden("无权操作他人的购物车");
        }
        //删除数据库
        cartMapper.deleteById(id);
        //删除哈希Redis缓存
        cartUtil.removeCacheField(userId, cart.getProductId());
        return getCart(userId);
    }

    /**
     * 结算预览
     * @param userId 用户id
     * @param cartIds 选择的购物车id集合,传入null默认已经勾选的物品
     * @return 结算预览vo
     */
    @Override
    public CartPreviewVO preview(Long userId, List<Long> cartIds) {
        //该用户查询购物车记录
        List<Cart> all = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
        //判断传入的cartIds 有传入的id按照ID筛选,没有取数据库已经勾选的项目
        List<Cart> picked;
        if(cartIds!=null&&!cartIds.isEmpty()){
            Set<Long> idSet =new HashSet<>(cartIds);
            picked = all.stream().filter(c -> idSet.contains(c.getId())).toList();
        }else{
            picked=all.stream().filter(c->c.getSelected()!=null&&c.getSelected()==Constants.FLAG_YES).toList();
        }
        //没有选中商品抛出异常
        if(picked.isEmpty()){
            throw BizException.badRequest("请选择要结算的商品");
        }
        //提取所有ID,批量调用修饰器查询商品信息,解决循环依赖n+1问题
        List<Long> productIds = picked.stream().map(Cart::getProductId).distinct().toList();
        //批量修饰productvo转map,kay为商品id,便于查找
        Map<Long, FACProductVO> productMap = productDecorator.decorateBatch(productIds).stream().collect(Collectors.toMap(FACProductVO::getId, Function.identity(), (a, b) -> a));
        //遍历选中的购物车项目,检验商品的可用性,累计总数量,总金额,手机卖家id
        List<CartItemVO> items=new ArrayList<>();
        int totalQty=0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> sellerIds=new HashSet<>();

        for(Cart c:picked){
            FACProductVO p = productMap.get(c.getProductId());
            if(p==null){
                throw BizException.notFound("商品不存在或已删除");
            }
            //检验商品是否可以购买,下架或者告罄直接报错
            if(!Boolean.TRUE.equals(p.getIsAvailable())){
                throw BizException.notFound("("+p.getTitle()+")已下架或库存不足,请先移除");
            }
            CartItemVO vo=cartUtil.toItemVO(c,p);
            items.add(vo);
            totalQty+=c.getQuantity();
            totalAmount=totalAmount.add(vo.getSubtotal());
            if(p.getSellerId()!=null){
                sellerIds.add(p.getSellerId());
            }
        }
        totalAmount=totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);//折扣
        int sellerCount =sellerIds.size();
        List<Long> ids = items.stream().map(CartItemVO::getId).toList();
        //组装
        return new CartPreviewVO(items,totalQty,totalAmount,discount,totalAmount.subtract(discount),sellerCount,sellerCount==1,ids);
    }

    //下单后清理购物车,供订单模块使用
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCartItems(Long userId, List<Long> cartIds) {
        if(cartIds==null||cartIds.isEmpty()){
            return;
        }
        // 先查询目标购物项，拿到productId，后续用来删除Redis缓存；同时加上userId防止越权删别人数据
        List<Cart> targets = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartIds));
        if (targets.isEmpty()) {
            return;
        }
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId,userId)
                .in(Cart::getId,cartIds));
        for(Cart c :targets){
            cartUtil.removeCacheField(userId,c.getProductId());
        }
    }


}
