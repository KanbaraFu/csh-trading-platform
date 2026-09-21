package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper {

    /**
     * 根据ID查询用户完整资料
     */
    User selectUserProfileById(@Param("id") Long id);

    /**
     * 更新个人资料（仅更新可编辑字段）
     */
    int updateUserProfile(User user);

    /**
     * 新增用户（对应「增」功能）
     */
    int insertUser(User user);

    /**
     * 根据ID删除用户（对应「删」功能）
     */
    int deleteUserById(@Param("id") Long id);

}
