package com.campus.secondhand.service;

import com.campus.secondhand.dto.UserCreateDTO;
import com.campus.secondhand.dto.UserProfileUpdateDTO;
import com.campus.secondhand.vo.UserStatsVO;
import com.campus.secondhand.vo.UserVO;

/**
 * 个人中心（用户资料）服务：提供资料的增删改查，以及个人中心统计。
 */
public interface UserProfileService {

    /**
     * 查：按 userId 查询个人资料（完整字段，不含密码）
     */
    UserVO getProfileById(Long userId);

    /**
     * 增：新增用户
     */
    UserVO createUser(UserCreateDTO createDTO);

    /**
     * 改：更新指定用户的资料，返回更新后的完整信息
     */
    UserVO updateProfile(Long userId, UserProfileUpdateDTO updateDTO);

    /**
     * 删：删除指定用户
     */
    void deleteUser(Long userId);

    /**
     * 统计：我的订单 / 待付款 / 收藏 / 购物车 / 未读消息
     */
    UserStatsVO getUserStats(Long userId);

}
