package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.PageResult;
import com.campus.secondhand.dto.OrderCreateDTO;
import com.campus.secondhand.dto.OrderQueryDTO;
import com.campus.secondhand.pojo.Address;
import com.campus.secondhand.pojo.Order;
import com.campus.secondhand.pojo.OrderItem;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.*;
import com.campus.secondhand.mapper.OrderMapper;
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

    @Override
    @Transactional
    public List<OrderVO> createOrder(OrderCreateDTO orderCreateDTO, Long buyId) {
        if (CollectionUtils.isEmpty(orderCreateDTO.getItems())) {
            throw BizException.badRequest("下单商品不能为空！");
        }
        List<Order> orders = new ArrayList<>();         // 订单集合
        List<OrderItem> orderItems = new ArrayList<>(); // 订单详情集合
        List<OrderVO> orderVOS = new ArrayList<>();     // 返回前端的订单结果集合

        // 获取id对应的商品信息
        List<OrderDetailVO> orderDetailVOS = orderMapper.selectProductByOrderCreateDTO(orderCreateDTO);
        if (orderDetailVOS.size() != orderCreateDTO.getItems().size()) {
            String message = "订单创建失败！该商品已经售卖完成或者已下架！";
            throw BizException.badRequest(message);
        }

        // TODO 获取地址（这里作为临时调用，因为获取的不一定是Address对象，有可能是addressVO对象。需要日后看看是否需要AddressVO，如果没有就自己创建）
        Address address = addressService.getById(orderCreateDTO.getAddressId());
        if (address == null) {
            throw BizException.badRequest("下单商品不能为空");
        }
        if (!address.getUserId().equals(buyId)) {
            throw BizException.badRequest("您无权使用该收货地址！");
        }

        // 生成地址快照
        String addressSnapshot =
                address.getReceiverName() + " " +
                address.getPhone() + " " +
                address.getRegion() + " " +
                address.getDetail();

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


            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 生成订单号
            String orderNo =
                    now.getYear() +
                    now.getMonthValue() +
                    now.getDayOfMonth() +
                    now.getHour() +
                    now.getMinute() +
                    // 随机数4位
                    String.valueOf(new Random().nextInt(1000, 10000));
            order.setOrderNo(orderNo);                  // 订单号
            order.setBuyerId(buyId);                   // 买家id（即token中的用户id
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
        System.out.println(row1);
        if (row1 <= 0) {
            String message = "插入订单列表失败！";
            throw BizException.badRequest(message);
        }

        String buyerAvatar = userService.getById(buyId).getAvatar();

        // 卖家头像（因为存在多个订单，所以头像会不止一个）
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
        if (row2 <= 0) {
            String message = "插入订单详细信息失败！";
            throw BizException.badRequest(message);
        }

        // 下单成功后，如果orderCreateDTO提供了cartIds的值，那就需要调用购物车模块中的清空购物车的方法
        if (!CollectionUtils.isEmpty(orderCreateDTO.getCartIds())) {
            cartService.removeByIds(orderCreateDTO.getCartIds());
        }

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
        if (orderIds == null) {
            String message = "订单id获取失败!";
            throw BizException.notFound(message);
        }

        // 通过订单ids查询订单列表及其订单详情
        List<OrderVO> orderVOS = orderMapper.selectOrderListByOrderIds(orderIds.getRecords());
        if (orderVOS == null) {
            String message = "订单列表获取失败!";
            throw BizException.notFound(message);
        }

        // 获取订单中的商品数量，并将其设置在vo对象上的itemCount上
        orderVOS.forEach((orderVO) -> orderVO.setItemCount(orderVO.getItems().size()));

        // 获取订单id对应的status值（需要获取每个状态的数量来放进counts里）

        List<Map<String, Long>> results = orderMapper.selectAllStatus(orderQueryDTO,userId);
        if (results == null) {
            String message = "状态筛选获取失败！";
            throw BizException.notFound(message);
        }

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
}




