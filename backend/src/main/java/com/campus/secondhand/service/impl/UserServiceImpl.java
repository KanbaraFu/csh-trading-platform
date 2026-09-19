package com.campus.secondhand.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.utils.PasswordUtil;
import com.campus.secondhand.utils.RedisUtil;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.LoginVO;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
* @author violet
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-09-17 15:08:48
*/
@Slf4j
@Service
public class UserServiceImpl extends CrudRepository<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TokenUtil tokenUtil;

    private static final String CODE_PREFIX = "register:code:";

    private static final String USER_CACHE_PREFIX = "user:info:";

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /*用户登录*/
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1、根据账号查询用户
        String phone = StringUtils.isNotBlank(loginDTO.getPhone()) ? loginDTO.getPhone() : loginDTO.getUsername();
        String password = loginDTO.getPassword();
        // 根据手机号查找
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        // 非空校验
        if (user == null) {
            throw new BizException("用户不存在，请先注册！");
        }
        // 2、密码校验
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new BizException("密码错误，请重试！");
        }
        // 3、校验通过后生成token并缓存用户信息
        return generateLoginVOAndCache(user);
    }

    /**
     * 生曾token、缓存用户信息
     * @param user
     * @return
     */
    private LoginVO generateLoginVOAndCache(User user) {

        // 1、成成token,7天过期
        String token = tokenUtil.createToken(user.getId());
        // 2、缓存用户信息
        user.setPassword(null);
        redisUtil.set(USER_CACHE_PREFIX + user.getId(), user,7, TimeUnit.DAYS);
        // 3、返回LoginVO
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        return loginVO;
    }

    /*用户注册*/
    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        String phone = registerDTO.getPhone();
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();
        String inputCode = registerDTO.getCode();

        // 1、手机号合法性校验
        if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BizException("手机号格式有误！");
        }

        // 2、密码校验
        if (StringUtils.isBlank(password) || password.length() < 6 || password.length() > 20) {
            throw new BizException("密码长度必须在6~20位之间！");
        }
        if (!password.equals(confirmPassword)) {
            throw new BizException("两次密码输入不一致！");
        }

        // 3、验证码检验
        Object cacheCode = redisUtil.get(CODE_PREFIX + phone);
        if (cacheCode == null || !inputCode.equals(cacheCode.toString())) {
            throw new BizException("验证码错误或已过期！");
        }

        // 4、检验手机号是否已被注册
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (count > 0) {
            throw new BizException("该手机号已被注册！");
        }

        // 5、检验成功后写入用户记录
        User user = new User();
        user.setPhone(phone);
        user.setUsername(phone);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.insert(user);
        System.out.println("插入影响行数：" + rows);

        if (rows <= 0) {
            throw new BizException("注册失败，数据库写入异常！");
        }

        // 6. 删除已使用的验证码
        redisUtil.delete(CODE_PREFIX + phone);

        // 7、注册成功后直接生成token，并将用户信息写入缓存，注册完直接登录
        try {
            generateLoginVOAndCache(user);
        } catch (Exception e) {
            log.error("注册成功，但自动登录缓存失效，请手动登录！", e);
        }

    }

    /*验证码*/
    @Override
    public String sendCode(String phone) {
        if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BizException("手机号格式有误！");
        }
        String code = String.format("%06d",new Random().nextInt(1000000));
        redisUtil.set(CODE_PREFIX + phone, code,5, TimeUnit.MINUTES);
        return code;
    }

    /*退出登录*/
    @Override
    public void logout(String token) {
        if(StringUtils.isNotBlank(token)){
            if (token.startsWith("Bearer")) {
                token = token.substring(7);
            }
            tokenUtil.removeToken(token);
        }
    }

}




