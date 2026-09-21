package com.campus.secondhand.utils;

import com.campus.secondhand.common.BizException;

public class ExceptionUtil {

    public  static void isTrue(Boolean flag,String msg){
        if(flag){
            throw  new BizException(msg);
        }
    }

    /* 是否请求失败 */
    public static void isBadRequest(Boolean flag,String msg) {
        if(flag){
            throw BizException.badRequest(msg);
        }
    }

    /* 是否处于登录状态 */
    public static void isUnAuthorized(Boolean flag,String msg) {
        if(flag){
            throw BizException.unauthorized(msg);
        }
    }

    /* 是否访问了未授权的资源 */
    public static void isForbidden(Boolean flag,String msg) {
        if(flag){
            throw BizException.forbidden(msg);
        }
    }

    /* 是否未找到页面 */
    public static void isNotFound(Boolean flag,String msg) {
        if(flag){
            throw BizException.notFound(msg);
        }
    }
}
