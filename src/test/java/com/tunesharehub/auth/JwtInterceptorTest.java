package com.tunesharehub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class JwtInterceptorTest {

    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final JwtInterceptor jwtInterceptor = new JwtInterceptor(jwtProvider);
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final HandlerMethod handler = mock(HandlerMethod.class);

    @Test
    void publicPlaylistListDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void playlistCreateStillRequiresToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/playlists");

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, handler))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증 토큰이 필요합니다.");
    }
}
