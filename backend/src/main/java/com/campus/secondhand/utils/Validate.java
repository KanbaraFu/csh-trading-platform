package com.campus.secondhand.utils;

import com.campus.secondhand.common.BizException;

public class Validate {
    /**
     * 其他错误异常，code:400
     * @param target
     * @param message
     */
    public static void verify(boolean target,String message){
        if(target){
            throw new BizException(400,message);
        }
    }

    /**
     * 资源不存在异常，code:404
     * @param target
     * @param message
     */
    public static void notFound(boolean target,String message){
        if(target){
            throw BizException.notFound(message);
        }
    }
}
