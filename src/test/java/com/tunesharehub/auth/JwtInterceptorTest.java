package com.tunesharehub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void publicPlaylistDetailDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void publicPlaylistDetailWithTrailingSlashDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10/");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void publicPlaylistTracksDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10/tracks");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void publicPlaylistCommentsDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10/comments");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void publicPlaylistSimilarDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10/similar");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void healthDoesNotRequireToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        verify(jwtProvider, never()).parseAccessToken(anyString());
    }

    @Test
    void malformedPublicPlaylistPathStillRequiresToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/latest");

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, handler))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증 토큰이 필요합니다.");
    }

    @Test
    void publicEndpointParsesTokenWhenAuthorizationHeaderExists() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/playlists/10");
        request.addHeader("Authorization", "Bearer access-token");
        AuthUser authUser = new AuthUser(1L, "user@example.com", "alice", "USER");
        when(jwtProvider.parseAccessToken("access-token")).thenReturn(authUser);

        boolean result = jwtInterceptor.preHandle(request, response, handler);

        assertThat(result).isTrue();
        assertThat(request.getAttribute(JwtInterceptor.AUTH_USER_ATTRIBUTE)).isEqualTo(authUser);
    }

    @Test
    void sessionMeRequiresToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/session/me");

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, handler))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증 토큰이 필요합니다.");
    }

    @Test
    void playlistCreateStillRequiresToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/playlists");

        assertThatThrownBy(() -> jwtInterceptor.preHandle(request, response, handler))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증 토큰이 필요합니다.");
    }
}
