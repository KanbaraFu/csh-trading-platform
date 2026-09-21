package com.campus.secondhand.service.impl;

import com.campus.secondhand.common.BizException;
import com.campus.secondhand.dto.UserCreateDTO;
import com.campus.secondhand.dto.UserProfileUpdateDTO;
import com.campus.secondhand.mapper.UserProfileMapper;
import com.campus.secondhand.mapper.UserStatsMapper;
import com.campus.secondhand.pojo.User;
import com.campus.secondhand.service.UserProfileService;
import com.campus.secondhand.utils.PasswordUtil;
import com.campus.secondhand.vo.UserStatsVO;
import com.campus.secondhand.vo.UserVO;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private UserStatsMapper userStatsMapper;

    /**
     * 查：查询用户资料并转 VO（不含密码）
     */
    @Override
    public UserVO getProfileById(Long userId) {
        return toUserVO(requireUser(userId));
    }

    /**
     * 增：新增用户。
     * 密码必须加密后落库；昵称、状态、时间等缺省值在此补齐。
     */
    @Override
    public UserVO createUser(UserCreateDTO createDTO) {
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        // 密码加密存储，禁止明文落库
        user.setPassword(PasswordUtil.encrypt(createDTO.getPassword()));
        // 昵称缺省使用账号
        if (StringUtils.isBlank(user.getNickname())) {
            user.setNickname("用户" + createDTO.getUsername());
        }
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        try {
            userProfileMapper.insertUser(user);
        } catch (DuplicateKeyException e) {
            // 账号唯一索引冲突，转成可读的业务提示，避免被兜底成「系统异常！」
            throw new BizException("该账号已存在");
        }
        return toUserVO(userProfileMapper.selectUserProfileById(user.getId()));
    }

    /**
     * 改：更新个人资料。
     * 先确认用户存在，避免「新旧值相同导致 affected rows=0」被误判为更新失败；
     * 传哪些字段就更新哪些字段（XML 动态 SQL 控制）。
     */
    @Override
    public UserVO updateProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        // 先确认用户存在
        requireUser(userId);

        User user = new User();
        user.setId(userId);
        // 仅拷贝 DTO 中的字段，null 字段由 XML 的动态 <if> 跳过
        BeanUtils.copyProperties(updateDTO, user);
        userProfileMapper.updateUserProfile(user);

        // 返回更新后的完整信息，供前端刷新 userStore
        return toUserVO(userProfileMapper.selectUserProfileById(userId));
    }

    /**
     * 删：删除指定用户（先校验存在，避免静默删除 0 行）
     */
    @Override
    public void deleteUser(Long userId) {
        requireUser(userId);
        userProfileMapper.deleteUserById(userId);
    }

    /**
     * 统计：我的订单 / 待付款 / 收藏 / 购物车 / 未读消息。
     * 采用轻量化方案，直接由 UserStatsMapper 聚合各表条数，不额外建立实体。
     */
    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO stats = new UserStatsVO();
        stats.setOrderTotal(userStatsMapper.countOrders(userId));
        stats.setPendingPay(userStatsMapper.countPendingPay(userId));
        stats.setFavoriteCount(userStatsMapper.countFavorites(userId));
        stats.setCartCount(userStatsMapper.countCartItems(userId));
        stats.setUnreadCount(userStatsMapper.countUnreadMessages(userId));
        return stats;
    }

    /**
     * 校验用户存在并返回实体，不存在则抛业务异常
     */
    private User requireUser(Long userId) {
        if (userId == null) {
            throw new BizException("用户不存在");
        }
        User user = userProfileMapper.selectUserProfileById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 实体转 VO（UserVO 不含密码字段，天然脱敏）
     */
    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

}
