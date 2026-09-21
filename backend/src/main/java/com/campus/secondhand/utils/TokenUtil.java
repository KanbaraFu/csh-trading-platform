package com.campus.secondhand.utils;

import com.campus.secondhand.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 登录 Token 工具类：UUID Token + Redis，不使用 JWT。
 *
 */
@Component
public class TokenUtil {

    private final RedisUtil redisUtil;

    public TokenUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 生成新 Token 并写入 Redis。
     *
     * @param userId 登录用户的 id
     * @return 去掉横线的 UUID，共 32 位
     */
    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisUtil.set(Constants.tokenKey(token), String.valueOf(userId), Constants.TOKEN_TTL);
        return token;
    }

    /**
     * 根据 Token 取 userId。
     *
     * @return 有效则返回 userId，Token 为空 / 不存在 / 已过期 / 内容非法时返回 null
     */
    public Long getUserId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String value = redisUtil.getString(Constants.tokenKey(token));
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            // 缓存被人工改坏了，当作 Token 无效处理，不让脏数据把请求打挂
            return null;
        }
    }

    /**
     * Token 是否有效。
     */
    public boolean validate(String token) {
        return getUserId(token) != null;
    }

    /**
     * 续期：每次有效请求把过期时间重置为 7 天，实现「活跃用户不掉线」。
     */
    public void refresh(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        redisUtil.expire(Constants.tokenKey(token), Constants.TOKEN_TTL);
    }

    /**
     * 删除 Token，用于退出登录。
     */
    public void removeToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        redisUtil.delete(Constants.tokenKey(token));
    }

    /**
     * 校验并续期，返回 userId。
     *
     * @return 无效返回 null
     */
    public Long getUserIdAndRefresh(String token) {
        Long userId = getUserId(token);
        if (userId != null) {
            refresh(token);
        }
        return userId;
    }
}
