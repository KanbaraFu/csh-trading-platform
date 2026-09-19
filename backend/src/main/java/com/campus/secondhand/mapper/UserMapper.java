package com.campus.secondhand.mapper;

import com.campus.secondhand.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import jakarta.validation.constraints.NotBlank;

public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@NotBlank(message = "账号不能为空") String username);


    User selectUserById(@Param("userId") Long userId);
}




