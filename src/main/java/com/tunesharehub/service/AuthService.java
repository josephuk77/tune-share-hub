package com.tunesharehub.service;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.InvalidTokenException;
import com.tunesharehub.auth.JwtProvider;
import com.tunesharehub.dto.LoginRequest;
import com.tunesharehub.dto.LoginResponse;
import com.tunesharehub.dto.UserSummaryResponse;
import com.tunesharehub.entity.RefreshToken;
import com.tunesharehub.entity.User;
import com.tunesharehub.exception.LoginFailedException;
import com.tunesharehub.mapper.RefreshTokenMapper;
import com.tunesharehub.mapper.UserMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final ZoneId SERVER_ZONE_ID = ZoneId.of("UTC");

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtProvider jwtProvider;

    public AuthService(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper, JwtProvider jwtProvider) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findActiveByEmail(request.email());
        if (user == null || !isPasswordMatched(request.password(), user.getPasswordHash())) {
            throw new LoginFailedException();
        }

        return issueTokens(toAuthUser(user));
    }

    @Transactional
    public LoginResponse refresh(String refreshTokenValue) {
        AuthUser tokenUser = jwtProvider.parseRefreshToken(refreshTokenValue);
        int revokedCount = refreshTokenMapper.revokeActiveByTokenValue(refreshTokenValue);
        if (revokedCount != 1) {
            throw new InvalidTokenException("유효하지 않은 refresh token입니다.");
        }

        User user = userMapper.findActiveById(tokenUser.userId());
        if (user == null) {
            throw new InvalidTokenException("유효하지 않은 refresh token입니다.");
        }

        return issueTokens(toAuthUser(user));
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        jwtProvider.parseRefreshToken(refreshTokenValue);
        refreshTokenMapper.revokeActiveByTokenValue(refreshTokenValue);
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(Long userId) {
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new InvalidTokenException("유효하지 않은 access token입니다.");
        }
        return toUserSummaryResponse(toAuthUser(user));
    }

    private LoginResponse issueTokens(AuthUser authUser) {
        String accessToken = jwtProvider.createAccessToken(authUser);
        String refreshToken = jwtProvider.createRefreshToken(authUser);
        saveRefreshToken(authUser.userId(), refreshToken);

        return new LoginResponse(
                accessToken,
                refreshToken,
                toUserSummaryResponse(authUser)
        );
    }

    private void saveRefreshToken(Long userId, String refreshTokenValue) {
        Instant expiresAt = jwtProvider.getRefreshTokenExpiresAt(refreshTokenValue);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenValue(refreshTokenValue);
        refreshToken.setExpiresAt(LocalDateTime.ofInstant(expiresAt, SERVER_ZONE_ID));
        refreshTokenMapper.insert(refreshToken);
    }

    private AuthUser toAuthUser(User user) {
        return new AuthUser(user.getUserId(), user.getEmail(), user.getNickname(), user.getRole());
    }

    private UserSummaryResponse toUserSummaryResponse(AuthUser authUser) {
        return new UserSummaryResponse(authUser.userId(), authUser.email(), authUser.nickname(), authUser.role());
    }

    private boolean isPasswordMatched(String password, String passwordHash) {
        try {
            return BCrypt.checkpw(password, passwordHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
