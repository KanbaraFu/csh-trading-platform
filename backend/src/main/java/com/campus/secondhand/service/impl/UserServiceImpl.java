package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.utils.JwtUtil;
import com.campus.secondhand.utils.PasswordUtil;
import com.campus.secondhand.utils.RedisUtil;
import com.campus.secondhand.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author violet
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-09-17 15:08:48
*/
@Service
public class UserServiceImpl extends CrudRepository<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1、根据账号查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("账号不存在！");
        }

        // 2、密码校验
        boolean ok = PasswordUtil.matches(loginDTO.getPassword(), user.getPassword());
        if (!ok) {
            throw new RuntimeException("密码错误！");
        }

        // 3、生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 4、token存入redis
        redisUtil.set("login:token:" + token, user.getId(),60*60*24);

        //5、返回vo
        return new LoginVO(token, user.getId(), user.getUsername());

    }

    @Override
    public void register(@Valid RegisterDTO registerDTO) {
        // 1、判断两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入密码不一致");
        }

        // 2、判断用户名是否存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new RuntimeException("该用户名已被注册！");
        }

        // 3、封装user，密码加密
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 加密
        user.setPassword(registerDTO.getPassword());
        user.setPhone(registerDTO.getPhone());

        // 4、加入数据库
        userMapper.insert(user);

    }

}




