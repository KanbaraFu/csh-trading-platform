package com.campus.secondhand.service;

import com.baomidou.mybatisplus.extension.repository.IRepository;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.vo.LoginVO;
import jakarta.validation.Valid;


/**
* @author violet
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-09-17 15:08:48
*/
public interface UserService extends IRepository<User> {

    /*登录*/
    LoginVO login(LoginDTO loginDTO);

    /*注册*/
    LoginVO register(RegisterDTO registerDTO);

    /*验证码*/
    String sendCode(String phone);

    /*退出登录*/
    void logout(String token); // 当前请求头中的token

}
