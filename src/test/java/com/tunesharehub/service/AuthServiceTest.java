package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tunesharehub.auth.InvalidTokenException;
import com.tunesharehub.auth.JwtProvider;
import com.tunesharehub.dto.UserSummaryResponse;
import com.tunesharehub.entity.User;
import com.tunesharehub.mapper.RefreshTokenMapper;
import com.tunesharehub.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void getCurrentUserReturnsActiveUser() {
        User user = activeUser();
        when(userMapper.findActiveById(1L)).thenReturn(user);

        UserSummaryResponse response = authService.getCurrentUser(1L);

        assertThat(response).isEqualTo(new UserSummaryResponse(1L, "alice@example.com", "alice", "USER"));
    }

    @Test
    void getCurrentUserRejectsMissingUser() {
        when(userMapper.findActiveById(1L)).thenReturn(null);

        assertThatThrownBy(() -> authService.getCurrentUser(1L))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 access token입니다.");
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("alice@example.com");
        user.setNickname("alice");
        user.setRole("USER");
        return user;
    }
}
