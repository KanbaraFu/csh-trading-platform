package com.campus.secondhand.common;

import java.time.Duration;

/**
 * 全局常量：统一返回码、接口前缀、Redis Key、缓存过期时间、业务状态值。
 */
public final class Constants {

    private Constants() {
        // 工具类，禁止实例化
    }

    /* ==================== 统一返回码 ==================== */

    /** 成功 */
    public static final int CODE_SUCCESS = 200;
    /** 参数错误 / 业务校验不通过 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 未登录或 Token 失效 */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 已登录但无权限操作该数据 */
    public static final int CODE_FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int CODE_NOT_FOUND = 404;
    /** 系统异常 */
    public static final int CODE_ERROR = 500;

    public static final String MSG_SUCCESS = "success";
    public static final String MSG_ERROR = "系统繁忙，请稍后重试";

    /* ==================== 接口 / 请求 ==================== */

    /** 所有接口统一前缀 */
    public static final String API_PREFIX = "/api";
    /** 拦截所有接口的 Ant 路径 */
    public static final String API_ALL_PATTERN = "/api/**";
    /** 登录 Token 所在的请求头名称，与前端 request.js 中的 config.headers.token 保持一致 */
    public static final String TOKEN_HEADER = "token";

    /* ==================== 分页 ==================== */

    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";
    public static final long DEFAULT_PAGE_NUM = 1L;
    public static final long DEFAULT_PAGE_SIZE = 10L;
    /** 单页最大条数，防止前端一次拉全表 */
    public static final long MAX_PAGE_SIZE = 100L;

    /* ==================== Redis Key 前缀 ==================== */

    /** 注册验证码：captcha:register:{phone} */
    public static final String REDIS_CAPTCHA_PREFIX = "captcha:register:";
    /** 登录 Token 对应 userId：token:login:{token} */
    public static final String REDIS_TOKEN_PREFIX = "token:login:";
    /** 用户信息缓存：user:profile:{userId} */
    public static final String REDIS_USER_PROFILE_PREFIX = "user:profile:";
    /** 分类列表缓存 */
    public static final String REDIS_CATEGORY_LIST = "category:list";
    /** 商品详情缓存：product:detail:{productId} */
    public static final String REDIS_PRODUCT_DETAIL_PREFIX = "product:detail:";
    /** 商品浏览量计数：product:view:count:{productId} */
    public static final String REDIS_PRODUCT_VIEW_COUNT_PREFIX = "product:view:count:";
    /** 购物车 Hash：cart:user:{userId} */
    public static final String REDIS_CART_PREFIX = "cart:user:";
    /** 下单防重锁：order:lock:user:{userId}:product:{productId} */
    public static final String REDIS_ORDER_LOCK_PREFIX = "order:lock:user:";
    /** 下单防重锁中连接 userId 与 productId 的固定片段 */
    public static final String REDIS_ORDER_LOCK_PRODUCT_INFIX = ":product:";
    /** 订单超时标记：order:timeout:{orderId} */
    public static final String REDIS_ORDER_TIMEOUT_PREFIX = "order:timeout:";
    /** 未读消息数：message:unread:{userId} */
    public static final String REDIS_MESSAGE_UNREAD_PREFIX = "message:unread:";
    /** 搜索热词 ZSet */
    public static final String REDIS_SEARCH_HOT_WORDS = "search:hot:words";
    /** 热门商品榜 ZSet */
    public static final String REDIS_STAT_HOT_PRODUCTS = "stat:hot:products";

    /* ==================== 缓存过期时间 ==================== */

    /** 注册验证码 5 分钟 */
    public static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    /** 登录 Token 7 天 */
    public static final Duration TOKEN_TTL = Duration.ofDays(7);
    /** 用户信息缓存 30 分钟 */
    public static final Duration USER_PROFILE_TTL = Duration.ofMinutes(30);
    /** 分类列表缓存 1 小时 */
    public static final Duration CATEGORY_TTL = Duration.ofHours(1);
    /** 商品详情缓存 10 分钟 */
    public static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(10);
    /** 购物车 7 天 */
    public static final Duration CART_TTL = Duration.ofDays(7);
    /** 下单防重锁 10 秒 */
    public static final Duration ORDER_LOCK_TTL = Duration.ofSeconds(10);
    /** 订单超时标记 30 分钟 */
    public static final Duration ORDER_TIMEOUT_TTL = Duration.ofMinutes(30);

    /* ==================== 业务状态值 ==================== */

    /** user.status：0 禁用 */
    public static final int USER_STATUS_DISABLED = 0;
    /** user.status：1 正常 */
    public static final int USER_STATUS_NORMAL = 1;

    /** product.status：0 下架 */
    public static final int PRODUCT_STATUS_OFF = 0;
    /** product.status：1 在售 */
    public static final int PRODUCT_STATUS_ON = 1;
    /** product.status：2 已售 */
    public static final int PRODUCT_STATUS_SOLD = 2;

    /** order.status：0 待支付 */
    public static final int ORDER_STATUS_UNPAID = 0;
    /** order.status：1 已支付 */
    public static final int ORDER_STATUS_PAID = 1;
    /** order.status：2 已发货 */
    public static final int ORDER_STATUS_DELIVERED = 2;
    /** order.status：3 已完成 */
    public static final int ORDER_STATUS_FINISHED = 3;
    /** order.status：4 已取消 */
    public static final int ORDER_STATUS_CANCELED = 4;

    /** cart.selected / address.is_default：0 否 */
    public static final int FLAG_NO = 0;
    /** cart.selected / address.is_default：1 是 */
    public static final int FLAG_YES = 1;

    /** message.is_read：0 未读 */
    public static final int MESSAGE_UNREAD = 0;
    /** message.is_read：1 已读 */
    public static final int MESSAGE_READ = 1;

    /* ==================== Redis Key 构建方法 ==================== */

    /** captcha:register:{phone} */
    public static String captchaKey(String phone) {
        return REDIS_CAPTCHA_PREFIX + phone;
    }

    /** token:login:{token} */
    public static String tokenKey(String token) {
        return REDIS_TOKEN_PREFIX + token;
    }

    /** user:profile:{userId} */
    public static String userProfileKey(Long userId) {
        return REDIS_USER_PROFILE_PREFIX + userId;
    }

    /** product:detail:{productId} */
    public static String productDetailKey(Long productId) {
        return REDIS_PRODUCT_DETAIL_PREFIX + productId;
    }

    /** product:view:count:{productId} */
    public static String productViewCountKey(Long productId) {
        return REDIS_PRODUCT_VIEW_COUNT_PREFIX + productId;
    }

    /** cart:user:{userId} */
    public static String cartKey(Long userId) {
        return REDIS_CART_PREFIX + userId;
    }

    /** order:lock:user:{userId}:product:{productId} */
    public static String orderLockKey(Long userId, Long productId) {
        return REDIS_ORDER_LOCK_PREFIX + userId + REDIS_ORDER_LOCK_PRODUCT_INFIX + productId;
    }

    /** order:timeout:{orderId} */
    public static String orderTimeoutKey(Long orderId) {
        return REDIS_ORDER_TIMEOUT_PREFIX + orderId;
    }

    /** message:unread:{userId} */
    public static String messageUnreadKey(Long userId) {
        return REDIS_MESSAGE_UNREAD_PREFIX + userId;
    }
}
