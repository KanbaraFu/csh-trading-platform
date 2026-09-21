package com.campus.secondhand.controller;

import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.vo.CaptchaVO;
import com.campus.secondhand.vo.LoginVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * 用户注册（注册成功后直接返回登录信息，前端可自动登录）
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = userService.register(registerDTO);
        return Result.success(loginVO);
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
     * 发送验证码（对齐前端 POST /api/auth/captcha）
     * body: { "phone": "13800000001" }
     */
    @PostMapping("/captcha")
    public Result<CaptchaVO> captcha(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = userService.sendCode(phone);
        return Result.success(new CaptchaVO(phone, code, 300));
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token) {
        userService.logout(token);
        return Result.success();
    }

}
