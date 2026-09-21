package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.repository.CrudRepository;
import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.dto.LoginDTO;
import com.campus.secondhand.dto.RegisterDTO;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.utils.PasswordUtil;
import com.campus.secondhand.utils.RedisUtil;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.LoginVO;
import com.campus.secondhand.vo.UserVO;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 手机号格式：1 开头，第二位 3-9，共 11 位 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 注册验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 用户信息缓存有效期（天） */
    private static final int USER_CACHE_DAYS = 7;

    /*用户登录*/
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1、根据账号查询用户（优先使用手机号）
        String phone = StringUtils.isNotBlank(loginDTO.getPhone()) ? loginDTO.getPhone() : loginDTO.getUsername();
        String password = loginDTO.getPassword();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .last("LIMIT 1"));
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
     * 生成 token、缓存用户信息（登录 / 注册 共用）
     * @param user 用户实体
     * @return 登录返回对象
     */
    private LoginVO generateLoginVOAndCache(User user) {
        // 1、生成token,7天过期
        String token = tokenUtil.createToken(user.getId());
        // 2、缓存用户信息（置空密码后缓存，避免密文外泄）
        user.setPassword(null);
        redisUtil.set(Constants.userProfileKey(user.getId()), user, USER_CACHE_DAYS, TimeUnit.DAYS);
        // 3、返回LoginVO
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        // 4、提供前端所需参数
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        loginVO.setUserVO(userVO);
        return loginVO;
    }

    /*用户注册*/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginVO register(RegisterDTO registerDTO) {
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
        // 确认密码为可选字段，仅在传入时做一致性校验
        if (StringUtils.isNotBlank(confirmPassword) && !password.equals(confirmPassword)) {
            throw new BizException("两次密码输入不一致！");
        }

        // 3、验证码检验
        Object cacheCode = redisUtil.get(Constants.captchaKey(phone));
        if (cacheCode == null || !inputCode.equals(cacheCode.toString())) {
            throw new BizException("验证码错误或已过期！");
        }

        // 4、检验手机号是否已被注册
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (count != null && count > 0) {
            throw new BizException("该手机号已被注册！");
        }

        // 5、检验成功后写入用户记录
        User user = new User();
        user.setPhone(phone);
        // 登录账号缺省使用手机号
        user.setUsername(StringUtils.isNotBlank(registerDTO.getUsername()) ? registerDTO.getUsername() : phone);
        user.setPassword(PasswordUtil.encrypt(password));
        // 昵称缺省按手机号后四位生成
        user.setNickname(StringUtils.isNotBlank(registerDTO.getNickname())
                ? registerDTO.getNickname()
                : "用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.insert(user);
        log.info("注册写入 user 表，影响行数={}, userId={}", rows, user.getId());

        if (rows <= 0) {
            throw new BizException("注册失败，数据库写入异常！");
        }

        // 6. 删除已使用的验证码
        redisUtil.delete(Constants.captchaKey(phone));

        // 7、注册成功后直接生成token并缓存用户信息，注册完直接登录
        return generateLoginVOAndCache(user);
    }

    /*验证码*/
    @Override
    public String sendCode(String phone) {
        if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BizException("手机号格式有误！");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisUtil.set(Constants.captchaKey(phone), code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return code;
    }

    /*退出登录*/
    @Override
    public void logout(String token) {
        if (StringUtils.isNotBlank(token)) {
            if (token.startsWith("Bearer")) {
                token = token.substring(7);
            }
            tokenUtil.removeToken(token);
        }
    }

}
