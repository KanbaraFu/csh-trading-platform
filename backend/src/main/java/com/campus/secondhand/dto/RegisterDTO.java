package com.campus.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册入参对象
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    private String phone;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}