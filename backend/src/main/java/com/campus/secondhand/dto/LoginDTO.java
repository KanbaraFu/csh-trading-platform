package com.campus.secondhand.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录入参对象
 */
@Data
public class LoginDTO {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 手机号（登录时优先使用该字段）
     */
    private String phone;
}