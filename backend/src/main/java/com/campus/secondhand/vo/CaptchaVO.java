package com.campus.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码返回对象（演示环境直接返回验证码，便于前端自动填入）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {
    private String phone;
    private String code;
    private Integer expireSeconds;
}
