package com.campus.secondhand.utils;

public class UserContextUtil {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    /**
     * 清理当前用户信息
     */
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }

}
