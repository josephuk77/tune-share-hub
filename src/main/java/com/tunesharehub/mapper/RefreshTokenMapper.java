package com.tunesharehub.mapper;

import com.tunesharehub.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

    void insert(RefreshToken refreshToken);

    int revokeActiveByTokenValue(@Param("tokenValue") String tokenValue);
}
