package com.campus.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册入参对象
 */
@Data
public class RegisterDTO {
    /**
     * 登录账号（可选，缺省时使用手机号）
     */
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 确认密码（可选，传入时会与 password 做一致性校验）
     */
    private String confirmPassword;
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 昵称（可选，缺省时按手机号后四位生成）
     */
    private String nickname;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
