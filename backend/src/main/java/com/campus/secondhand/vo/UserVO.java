package com.campus.secondhand.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录账号（手机号）
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别 0未知 1男 2女
     */
    private Integer gender;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学院
     */
    private String college;

    /**
     * 学生认证状态 0未认证 1已认证
     */
    private Integer authStatus;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 账号状态 0禁用 1正常（前端可据此判断账号是否被封禁）
     */
    private Integer status;
}