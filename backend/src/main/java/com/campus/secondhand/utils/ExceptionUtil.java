package com.campus.secondhand.utils;

import com.campus.secondhand.common.BizException;

public class ExceptionUtil {

    public  static void isTrue(Boolean flag,String msg){
        if(flag){
            throw  new BizException(msg);
        }
    }
}
