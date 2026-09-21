package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderPayDTO;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.mapper.ProductMapper;
import com.campus.secondhand.pojo.*;
import com.campus.secondhand.service.*;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.utils.ExceptionUtil;
import com.campus.secondhand.utils.SendMessageUtil;
import com.campus.secondhand.vo.OrderDetailVO;
import com.campus.secondhand.vo.OrderItemVO;
import com.campus.secondhand.vo.OrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
* @author Lenovo
* @description 针对表【order(订单)】的数据库操作Service实现
* @createDate 2026-09-16 11:56:41
*/
@Service
public class OrderServiceImpl extends CrudRepository<OrderMapper, Order>
    implements OrderService{

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public List<OrderVO> createOrder(OrderCreateDTO orderCreateDTO, Long buyerId) {
        ExceptionUtil.isBadRequest(CollectionUtils.isEmpty(orderCreateDTO.getItems()), "下单商品不能为空！");

        List<Order> orders = new ArrayList<>();         // 订单集合
        List<OrderItem> orderItems = new ArrayList<>(); // 订单详情集合
        List<OrderVO> orderVOS = new ArrayList<>();     // 返回前端的订单结果集合

        // 获取id对应的商品信息
        List<OrderDetailVO> orderDetailVOS = orderMapper.selectProductByOrderCreateDTO(orderCreateDTO);
        ExceptionUtil.isBadRequest(orderDetailVOS.size() != orderCreateDTO.getItems().size(), "订单创建失败！该商品已经售卖完成或者已下架！");

        // TODO 获取地址（这里作为临时调用，因为获取的不一定是Address对象，有可能是addressVO对象。需要日后看看是否需要AddressVO，如果没有就自己创建）
        Address address = addressService.getById(orderCreateDTO.getAddressId());
        ExceptionUtil.isBadRequest(address == null, "地址不能为空！");
        ExceptionUtil.isForbidden(!address.getUserId().equals(buyerId), "您无权使用该收货地址！");

        // 生成地址快照
        String addressSnapshot =
                address.getReceiverName() + " " +
                address.getPhone() + " " +
                address.getRegion() + " " +
                address.getDetail();

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 获取卖家列表并去重，因为有可能有的商品可能属于同一卖家
        List<Long> sellerIds = orderDetailVOS.stream()
                .map(OrderDetailVO::getSellerId)    // 转换成sellerIds的List集合
                .distinct()                         // 去重
                .sorted()                           // 排序
                .toList();


        // 生成订单
        sellerIds.forEach(sellerId -> {
            Order order = new Order();

            // 通过同一个卖家获取商品价格
            BigDecimal totalAmount = orderDetailVOS.stream()
                    .filter(orderDetailVO -> sellerId.equals(orderDetailVO.getSellerId()))
                    .map(OrderDetailVO::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 生成订单号
            String orderNo = "" +
                    now.getYear() +
                    now.getMonthValue() +
                    now.getDayOfMonth() +
                    now.getHour() +
                    // 随机数4位
                    new Random().nextInt(1000, 10000);
            order.setOrderNo(orderNo);                  // 订单号
            order.setBuyerId(buyerId);                   // 买家id（即token中的用户id
            order.setSellerId(sellerId);                // 卖家id
            order.setTotalAmount(totalAmount);          // 总金额
            order.setPayAmount(totalAmount);            // 支付金额
            order.setAddressSnapshot(addressSnapshot);  // 地址快照
            order.setRemark(orderCreateDTO.getRemark());// 备注
            order.setCreateTime(now);
            order.setUpdateTime(now);
            orders.add(order);
        });

        // 插入新添加的订单列表
        int row1 = orderMapper.insertOrder(orders);
        ExceptionUtil.isBadRequest(row1 <= 0, "插入订单列表失败！");


        String buyerAvatar = userService.getById(buyerId).getAvatar();

        // 卖家头像（因为可能存在多个订单，而且都是不同卖家，所以头像会不止一个）
        Map<Long, String> sellerAvatarMap = userService.listByIds(sellerIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> StringUtils.isBlank(u.getAvatar()) ? "" : u.getAvatar()));

        orders.forEach(order -> {
            Long sellerId = order.getSellerId();
            String sellerAvatar = sellerAvatarMap.get(sellerId);

            OrderVO orderVO = new OrderVO();
            List<OrderItemVO> orderItemVOS = new ArrayList<>();

            // 添加订单对应的商品信息
            orderDetailVOS.stream()
                    .filter(orderDetailVO -> sellerId.equals(orderDetailVO.getSellerId()))
                    .forEach(orderDetailVO -> {
                        Long orderId = order.getId();
                        Long productId = orderDetailVO.getProductId();
                        String productTitle = orderDetailVO.getProductTitle();
                        String productCover = orderDetailVO.getProductCover();
                        BigDecimal price = orderDetailVO.getPrice();
                        BigDecimal totalAmount = orderDetailVO.getTotalAmount();
                        Integer quantity = orderDetailVO.getQuantity();

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrderId(orderId);
                        orderItem.setProductId(productId);
                        orderItem.setProductTitle(productTitle);
                        orderItem.setProductCover(productCover);
                        orderItem.setPrice(price);
                        orderItem.setQuantity(quantity);
                        orderItem.setTotalAmount(totalAmount);

                        OrderItemVO orderItemVO = new OrderItemVO();
                        orderItemVO.setProductId(productId);
                        orderItemVO.setProductTitle(productTitle);
                        orderItemVO.setProductCover(productCover);
                        orderItemVO.setPrice(price);
                        orderItemVO.setQuantity(quantity);
                        orderItemVO.setTotalAmount(totalAmount);

                        orderItems.add(orderItem);
                        orderItemVOS.add(orderItemVO);
                    });

            orderVO.setId(order.getId());
            orderVO.setOrderNo(order.getOrderNo());
            orderVO.setBuyerId(order.getBuyerId());
            orderVO.setSellerId(order.getSellerId());
            orderVO.setTotalAmount(order.getTotalAmount());
            orderVO.setPayAmount(order.getPayAmount());
            orderVO.setStatus(0);
            orderVO.setAddressSnapshot(order.getAddressSnapshot());
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setUpdateTime(order.getUpdateTime());
            orderVO.setRemark(order.getRemark());
            orderVO.setBuyerId(order.getBuyerId());
            orderVO.setBuyerAvatar(buyerAvatar);
            orderVO.setSellerId(sellerId);
            orderVO.setSellerAvatar(sellerAvatar);
            orderVO.setItems(orderItemVOS);

            orderVOS.add(orderVO);
        });

        // 插入订单详细表
        int row2 = orderMapper.insertOrderItem(orderItems);
        ExceptionUtil.isBadRequest(row2 <= 0, "插入订单详细信息失败！");


        // 获取orderId集合
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .toList();

        // 扣减库存和累加销量
        changeStockAndSalesCount(orderItems, now, Constants.ORDER_STATUS_UNPAID);

        // 下单成功后，如果orderCreateDTO提供了cartIds的值，那就需要调用购物车模块中的清空购物车的方法
        if (!CollectionUtils.isEmpty(orderCreateDTO.getCartIds())) {
            cartService.removeByIds(orderCreateDTO.getCartIds());
        }

        // 调用消息模块发送消息
        orderVOS.forEach(order ->
                sendMessageUtil.notifyOrderStatus(
                        Long.valueOf(order.getOrderNo()),
                        order.getBuyerId(),order.getSellerId(),
                        Constants.ORDER_STATUS_UNPAID));

        return orderVOS;
    }

    @Override
    public PageResult<OrderVO> getOrders(OrderQueryDTO orderQueryDTO, Long userId) {
        // 当前页码，如果没有参数就为默认值
        Long pageNum = orderQueryDTO.getPageNum() != null ? orderQueryDTO.getPageNum() : Constants.DEFAULT_PAGE_NUM;
        // 每页条数，如果没有参数就为默认值
        Long pageSize = orderQueryDTO.getPageSize() != null ? orderQueryDTO.getPageSize() : Constants.DEFAULT_PAGE_SIZE;

        // 开启分页
        Page<OrderVO> page = new Page<>(pageNum,pageSize);

        // 获取分页后的订单id列表
        IPage<Long> orderIds = orderMapper.selectOrderIdsByPage(page, orderQueryDTO,userId);
        ExceptionUtil.isNotFound(orderIds == null, "订单id获取失败!");

        // 通过订单ids查询订单列表及其订单详情
        List<OrderVO> orderVOS = orderMapper.selectOrderListByOrderIds(orderIds.getRecords());
        ExceptionUtil.isNotFound(orderVOS == null, "订单列表获取失败!");


        // 获取订单中的商品数量，并将其设置在vo对象上的itemCount上
        orderVOS.forEach((orderVO) -> orderVO.setItemCount(orderVO.getItems().size()));

        // 获取订单id对应的status值（需要获取每个状态的数量来放进counts里）

        List<Map<String, Long>> results = orderMapper.selectAllStatus(orderQueryDTO,userId);
        ExceptionUtil.isNotFound(results == null, "状态筛选获取失败！");


        Map<String, Long> orderCounts = new HashMap<>();

        Long counts = 0L;
        for (Map<String, Long> result : results) {
            // 当前状态的订单数量
            Long count = result.get("count");
            orderCounts.put(String.valueOf(result.get("status")), count);
            counts += count;
        }

        orderCounts.put("all", counts);

        return PageResult.of(page,orderVOS,orderCounts);
    }

    @Override
    @Transactional
    public void payOrder(OrderPayDTO orderPayDTO, Long orderId, Long buyerId) {

        Order order = orderMapper.selectById(orderId);
        ExceptionUtil.isNotFound(order == null,"订单不存在！");
        ExceptionUtil.isForbidden(!order.getBuyerId().equals(buyerId), "非常抱歉，您无权支付该订单！");
        ExceptionUtil.isBadRequest(order.getStatus().equals(Constants.ORDER_STATUS_PAID),"该订单已支付完成，无法重复支付！");

        String payMethod = orderPayDTO.getPayMethod();
        boolean isTurePayMethod =
                payMethod.equals("campus_card")
                        || payMethod.equals("wechat")
                        || payMethod.equals("alipay");
        ExceptionUtil.isBadRequest(!isTurePayMethod ,"未选择支付方式！");
        ExceptionUtil.isBadRequest(orderPayDTO == null || orderPayDTO.getPayMethod() == null || !isTurePayMethod,"未选择支付方式！");

        // TODO 支付防重锁

        //获取当前时间
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(Constants.FORMATTER), Constants.FORMATTER);

        // 更新订单的条件
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Order::getStatus, Constants.ORDER_STATUS_PAID)
                .set(Order::getPayTime, now)
                .set(Order::getPayMethod, payMethod)
                .set(Order::getUpdateTime, now)
                .eq(Order::getId, orderId)
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, Constants.ORDER_STATUS_UNPAID);

        // 更新订单信息
        int row = orderMapper.update(updateWrapper);
        ExceptionUtil.isBadRequest(row <= 0,"支付失败！");

        // 发送支付成功的消息
        sendMessageUtil.notifyOrderStatus(
                Long.valueOf(order.getOrderNo()),
                order.getBuyerId(),order.getSellerId(),
                Constants.ORDER_STATUS_PAID);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        ExceptionUtil.isNotFound(order == null,"订单不存在！");
        ExceptionUtil.isForbidden(!order.getSellerId().equals(sellerId), "非常抱歉，您无权对此订单进行发货！");
        ExceptionUtil.isBadRequest(order.getStatus().equals(Constants.ORDER_STATUS_DELIVERED),"该订单已发货，不能重复发货！");

        //获取当前时间
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(Constants.FORMATTER), Constants.FORMATTER);

        // 更新对应的订单状态
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Order::getStatus, Constants.ORDER_STATUS_DELIVERED)
                .set(Order::getUpdateTime, now)
                .eq(Order::getSellerId, sellerId)
                .eq(Order::getStatus, Constants.ORDER_STATUS_PAID)
                .eq(Order::getId, orderId);

        // 更新发货状态
        int row = orderMapper.update(updateWrapper);
        ExceptionUtil.isBadRequest(row <= 0,"支付失败！");

        // 发送发货成功的消息
        sendMessageUtil.notifyOrderStatus(
                Long.valueOf(order.getOrderNo()),
                order.getBuyerId(),order.getSellerId(),
                Constants.ORDER_STATUS_DELIVERED);

    }

    @Override
    public void confirmOrder(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        ExceptionUtil.isNotFound(order == null,"订单不存在！");
        ExceptionUtil.isForbidden(!order.getBuyerId().equals(buyerId), "非常抱歉，您无权对此订单进行收货！");
        ExceptionUtil.isBadRequest(order.getStatus().equals(Constants.ORDER_STATUS_FINISHED),"该订单已确认收货，无法重复收货！");

        //获取当前时间
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(Constants.FORMATTER), Constants.FORMATTER);

        // 更新对应的订单状态
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Order::getStatus, Constants.ORDER_STATUS_FINISHED)
                .set(Order::getUpdateTime, now)
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, Constants.ORDER_STATUS_DELIVERED)
                .eq(Order::getId, orderId);

        // 更新已收货状态
        int row = orderMapper.update(updateWrapper);
        ExceptionUtil.isBadRequest(row <= 0,"支付失败！");

        // 发送发货成功的消息
        sendMessageUtil.notifyOrderStatus(
                Long.valueOf(order.getOrderNo()),
                order.getBuyerId(),order.getSellerId(),
                Constants.ORDER_STATUS_FINISHED);
    }

    @Override
    public void cancelOrder(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        ExceptionUtil.isNotFound(order == null,"订单不存在！");
        ExceptionUtil.isForbidden(!order.getBuyerId().equals(buyerId), "非常抱歉，您无权取消此订单！");

        Integer status = order.getStatus();
        ExceptionUtil.isBadRequest(status.equals(Constants.ORDER_STATUS_CANCELED),"该订单已确认取消，无法重复取消！");
        boolean flag = status == Constants.ORDER_STATUS_DELIVERED       // 已发货
                ||  status == Constants.ORDER_STATUS_FINISHED;          // 已收货

        // 如果上面的flag成立，则表示该订单已经发货，无法再取消
        ExceptionUtil.isBadRequest(flag,"该订单已经发货，无法再取消");

        //获取当前时间
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(Constants.FORMATTER), Constants.FORMATTER);

        List<OrderItem> orderItems = orderItemService
                .list(new LambdaQueryWrapper<OrderItem>()
                        .select(OrderItem::getProductId, OrderItem::getQuantity)
                        .eq(OrderItem::getOrderId,orderId));

        // 恢复库存
        changeStockAndSalesCount(orderItems,now,Constants.ORDER_STATUS_CANCELED);

        // 更新对应的订单状态
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Order::getStatus, Constants.ORDER_STATUS_CANCELED)
                .set(Order::getUpdateTime, now)
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getId, orderId);

        // 更新已收货状态
        int row = orderMapper.update(updateWrapper);
        ExceptionUtil.isBadRequest(row <= 0,"支付失败！");

        // 发送发货成功的消息
        sendMessageUtil.notifyOrderStatus(
                Long.valueOf(order.getOrderNo()),
                order.getBuyerId(),order.getSellerId(),
                Constants.ORDER_STATUS_FINISHED);
    }

    /**
     * 用来对库存和销量进行改变
     * @param orderItems 订单id列表
     * @param now 现在时间
     * @param status ORDER_STATUS_UNPAID为扣减库存和增加销量，ORDER_STATUS_CANCELED为恢复库存和销量
     */
    public void changeStockAndSalesCount(List<OrderItem> orderItems, LocalDateTime now, Integer status) {

        // 获取对应订单详情表的商品id
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        // 获取订单对应的商品的对应库存
        Map<Long, Integer> productStockMap = productMapper.selectByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getStock));

        orderItems.forEach(orderItem -> {
            Product product = new Product();
            product.setStock(productStockMap.get(orderItem.getProductId()));

            // 扣减库存和累加销量的条件
            LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();

            updateWrapper.set(Product::getUpdateTime, now)                           // 商品的更新时间
                    .eq(Product::getId, orderItem.getProductId());

            if (status == Constants.ORDER_STATUS_UNPAID) {  // 扣减库存和增加销量
                updateWrapper.setDecrBy(Product::getStock, orderItem.getQuantity())  // 库存-数量
                        .setIncrBy(Product::getSalesCount, orderItem.getQuantity())  // 销量+数量
                        .eq(Product::getStatus, 1)                               // 检查是否处于可售卖状态
                        .ge(Product::getStock, orderItem.getQuantity());             // 库存大于购买数量时再减

                if (product.getStock() - orderItem.getQuantity() <= 0) { // 判断库存-数量会不会小于0，如果小于0需要设置状态为已售
                    updateWrapper.set(Product::getStatus, 2);
                }

                int row = productMapper.update(updateWrapper);
                ExceptionUtil.isBadRequest(row <= 0, "该商品已售完或已下架，下次早点下单哦！");
            }
            if (status == Constants.ORDER_STATUS_CANCELED) { // 恢复库存和销量
                updateWrapper.setIncrBy(Product::getStock, orderItem.getQuantity())
                        .setDecrBy(Product::getSalesCount, orderItem.getQuantity())
                        .set(Product::getStatus, 1);                                // 因为默认恢复了库存，所以可以直接转换为设置状态为在售

                int row = productMapper.update(updateWrapper);
                ExceptionUtil.isBadRequest(row <= 0, "恢复库存失败！");
            }
        });
    }
}
