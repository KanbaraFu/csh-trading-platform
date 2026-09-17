package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Argentina
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-09-16 17:24:17
* @Entity com.campus.secondhand.pojo.User
*/
public interface UserMapper extends BaseMapper<User> {
    User selectUserById(@Param("userId") Long userId);
}




