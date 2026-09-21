package com.campus.secondhand.controller;

import com.campus.secondhand.common.BizException;
import com.campus.secondhand.common.Constants;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.UserCreateDTO;
import com.campus.secondhand.dto.UserProfileUpdateDTO;
import com.campus.secondhand.service.UserProfileService;
import com.campus.secondhand.utils.TokenUtil;
import com.campus.secondhand.vo.UserStatsVO;
import com.campus.secondhand.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心：用户资料的增删改查 + 各项统计。
 *
 * <p>不使用登录拦截器，因此每个需要身份的方法都通过请求头 {@code token}
 * 解析当前用户 ID，统一收敛到 {@link #currentUserId(String)}。</p>
 */
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 增：新增用户
     * POST /api/user
     */
    @PostMapping
    public Result<UserVO> createUser(@RequestBody @Validated UserCreateDTO createDTO) {
        return Result.success(userProfileService.createUser(createDTO));
    }

    /**
     * 查：查询当前登录用户的个人资料
     * GET /api/user/me
     */
    @GetMapping("/me")
    public Result<UserVO> getMyProfile(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token) {
        return Result.success(userProfileService.getProfileById(currentUserId(token)));
    }

    /**
     * 查：个人中心各项统计条数（订单 / 待付款 / 收藏 / 购物车 / 未读消息）
     * GET /api/user/me/stats
     */
    @GetMapping("/me/stats")
    public Result<UserStatsVO> getMyStats(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token) {
        return Result.success(userProfileService.getUserStats(currentUserId(token)));
    }

    /**
     * 改：修改当前登录用户的个人资料
     * PUT /api/user/me
     */
    @PutMapping("/me")
    public Result<UserVO> updateMyProfile(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token,
                                          @RequestBody @Validated UserProfileUpdateDTO updateDTO) {
        return Result.success(userProfileService.updateProfile(currentUserId(token), updateDTO));
    }

    /**
     * 删：删除当前登录用户
     * DELETE /api/user/me
     */
    @DeleteMapping("/me")
    public Result<Void> deleteMyProfile(@RequestHeader(value = Constants.TOKEN_HEADER, required = false) String token) {
        userProfileService.deleteUser(currentUserId(token));
        return Result.success();
    }

    /**
     * 从请求头 token 解析当前登录用户 ID（替代登录拦截器的线程上下文方案）。
     * token 缺失、失效或过期时抛出 401，交由全局异常处理返回。
     */
    private Long currentUserId(String token) {
        Long userId = tokenUtil.getUserId(token);
        if (userId == null) {
            throw BizException.unauthorized("登录已过期，请重新登录");
        }
        return userId;
    }

}
