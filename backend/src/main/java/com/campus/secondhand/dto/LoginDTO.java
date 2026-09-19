package com.campus.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录入参对象
 */
@Data
public class LoginDTO {
    /**
     * 手机号（登录时优先使用该字段）
     */
    @JsonProperty("phone")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @JsonProperty("password")
    private String password;

    @JsonProperty("username")
    private String username;
}