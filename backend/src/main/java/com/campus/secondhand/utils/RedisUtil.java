package com.campus.secondhand.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具类：把 {@link RedisTemplate} 的样板代码收敛到一处，业务里只调本类方法。
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* ==================== 通用 Key 操作 ==================== */

    /**
     * 删除 Key。
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除 Key，返回实际删除的数量。
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * Key 是否存在。
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间。
     */
    public Boolean expire(String key, Duration ttl) {
        return redisTemplate.expire(key, ttl);
    }

    /**
     * 获取剩余过期时间，单位秒。返回 -1 表示永久，-2 表示 Key 不存在。
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /* ==================== String（对象缓存） ==================== */

    /**
     * 写入缓存，不过期（用于搜索热词/浏览量这类需要长期保留的数据）。
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入缓存并设置过期时间，最常用的一个方法。
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取缓存原始值，取不到返回 null。
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 按指定类型读取缓存。
     *
     * <p>因为是 JSON 序列化，只要缓存里存的确实是该类型的对象，
     * 有 {@code @class} 类型信息就能还原。数字类型请优先用
     * {@link #getLong}/{@link #getInt}，避免 Integer/Long 不匹配。</p>
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new IllegalStateException(
                "缓存值类型不匹配：key=" + key + "，期望=" + type.getName() + "，实际=" + value.getClass().getName());
    }

    /**
     * 读取字符串值，null 安全。
     */
    public String getString(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 读取 Long 值，兼容缓存里是 Integer / String 的情况。
     */
    public Long getLong(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 读取 Integer 值，兼容缓存里是 Long / String 的情况。
     */
    public Integer getInt(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    /**
     * Key 不存在时才写入，返回是否写入成功。
     *
     * <p>典型的「下单防重锁」用法：</p>
     * <pre>
     * Boolean locked = redisUtil.setIfAbsent(Constants.orderLockKey(userId, productId), 1, Constants.ORDER_LOCK_TTL);
     * if (Boolean.FALSE.equals(locked)) {
     *     throw BizException.badRequest("操作太频繁，请稍后再试");
     * }
     * </pre>
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    /* ==================== 计数器 ==================== */

    /**
     * 自增 1，返回自增后的值。Key 不存在时会自动从 0 开始，常用于商品浏览量。
     */
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /* ==================== Hash（购物车） ==================== */

    /**
     * 写 Hash 的一个字段，field 通常是 productId。
     */
    public void hSet(String key, Object field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public Object hGet(String key, Object field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Boolean hHasKey(String key, Object field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 取出整个 Hash，即购物车全部条目。
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Long hDel(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * Hash 字段自增，可用于购物车「加数量」。
     */
    public Long hIncrBy(String key, Object field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /* ==================== Set ==================== */

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /* ==================== ZSet（搜索热词 / 热门商品榜） ==================== */

    /**
     * 添加成员并指定分数，已存在则覆盖分数。
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 分数自增，返回自增后的分数。搜索热词每被搜一次就 +1，用这个最合适。
     */
    public Double zIncrBy(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 按分数倒序取区间成员（0 开始，-1 表示最后），即排行榜 topN。
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 按分数倒序取区间成员，同时带上分数。
     */
    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public void set(String key, Long id, int i) {

    }
}
