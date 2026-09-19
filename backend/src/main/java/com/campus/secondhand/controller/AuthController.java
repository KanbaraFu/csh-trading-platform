package com.campus.secondhand.controller;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.vo.LoginVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService  userService;

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpSession session, HttpServletResponse response) {
        LoginVO loginVO = userService.login(loginDTO);
        session.setAttribute("token", loginVO.getToken());
        Cookie cookie = new Cookie("token", loginVO.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Constants.TOKEN_TTL.getSeconds());
        response.addCookie(cookie);
        return Result.success(loginVO);
    }

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    /**
     * 验证码
     * @param phone
     * @return
     */
    @GetMapping("/code")
    public Result<String> sendCode(@RequestParam("phone") String phone) {
        String code = userService.sendCode(phone);
        return Result.success(code);
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        userService.logout(token);
        return Result.success();
    }

}
