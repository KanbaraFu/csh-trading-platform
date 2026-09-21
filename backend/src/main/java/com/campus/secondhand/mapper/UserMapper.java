package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author violet
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-09-17 15:08:48
* @Entity com.campus.secondhand.pojo.User
*/
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按登录账号查询用户
     */
    User selectByUsername(String username);

}
