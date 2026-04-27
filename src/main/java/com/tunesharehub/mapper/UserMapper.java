package com.tunesharehub.mapper;

import com.tunesharehub.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findActiveByEmail(@Param("email") String email);

    User findActiveById(@Param("userId") Long userId);
}
