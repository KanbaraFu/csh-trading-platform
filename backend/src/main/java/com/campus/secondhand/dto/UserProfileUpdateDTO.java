package com.campus.secondhand.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 个人资料更新入参。
 *
 * <p>所有字段均为可选：既支持「资料编辑」整体提交，也支持只改头像等局部更新，
 * 具体更新哪些列由 UserProfileMapper.xml 的动态 &lt;if&gt; 决定。</p>
 */
@Setter
@Getter
public class UserProfileUpdateDTO {

    /**
     * 昵称（可选，最长 12 位）
     */
    @Size(max = 12, message = "昵称长度不能超过12位")
    private String nickname;

    /**
     * 手机号（可选）
     */
    @Size(max = 11, message = "手机号格式不正确")
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
     * 个人简介（可选，最长 80 字）
     */
    @Size(max = 80, message = "个人简介不能超过80字")
    private String bio;

    /**
     * 头像（可选，base64 或 URL，前端更换/恢复默认头像时提交）
     */
    private String avatar;

}
