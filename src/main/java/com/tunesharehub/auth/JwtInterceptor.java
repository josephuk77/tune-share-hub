package com.tunesharehub.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PUBLIC_PLAYLISTS_PATH = "/api/playlists";
    private static final int PLAYLIST_ID_SEGMENT_COUNT = 4;
    private static final int PLAYLIST_CHILD_SEGMENT_COUNT = 5;

    private final JwtProvider jwtProvider;

    public JwtInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null && isPublicEndpoint(request)) {
            return true;
        }
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("인증 토큰이 필요합니다.");
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length());
        request.setAttribute(AUTH_USER_ATTRIBUTE, jwtProvider.parseAccessToken(accessToken));
        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }

        String requestPath = getRequestPath(request);
        if (PUBLIC_PLAYLISTS_PATH.equals(requestPath)) {
            return true;
        }
        if (!requestPath.startsWith(PUBLIC_PLAYLISTS_PATH + "/")) {
            return false;
        }

        String[] segments = requestPath.split("/");
        if (segments.length == PLAYLIST_ID_SEGMENT_COUNT) {
            return isPositiveLong(segments[3]);
        }
        return segments.length == PLAYLIST_CHILD_SEGMENT_COUNT
                && isPositiveLong(segments[3])
                && ("tracks".equals(segments[4]) || "comments".equals(segments[4]));
    }

    private String getRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private boolean isPositiveLong(String value) {
        try {
            return Long.parseLong(value) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
