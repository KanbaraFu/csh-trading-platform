package com.campus.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 个人中心「增」：新增用户入参。
 */
@Setter
@Getter
public class UserCreateDTO {

    /**
     * 登录账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 登录密码（明文传入，服务端加密后落库）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6~20位之间")
    private String password;

    /**
     * 昵称（可选，缺省用账号生成）
     */
    private String nickname;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 性别：0-不公开 1-男 2-女（可选）
     */
    private Integer gender;

    /**
     * 学院（可选）
     */
    private String college;

    /**
     * 学号（可选）
     */
    private String studentNo;

    /**
     * 个人简介（可选）
     */
    private String bio;

}
