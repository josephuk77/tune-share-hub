package com.tunesharehub.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern HEALTH_PATTERN = Pattern.compile("^/api/health/?$");
    private static final Pattern PUBLIC_PLAYLIST_LIST_PATTERN = Pattern.compile("^/api/playlists/?$");
    private static final Pattern PUBLIC_PLAYLIST_RANKING_PATTERN = Pattern.compile("^/api/playlists/ranking/?$");
    private static final Pattern PUBLIC_PLAYLIST_DETAIL_PATTERN = Pattern.compile("^/api/playlists/[1-9]\\d*/?$");
    private static final Pattern PUBLIC_PLAYLIST_CHILD_PATTERN =
            Pattern.compile("^/api/playlists/[1-9]\\d*/(tracks|comments|similar)/?$");
    private static final Pattern PUBLIC_SPOTIFY_TRACK_SEARCH_PATTERN =
            Pattern.compile("^/api/spotify/search/tracks/?$");

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
        return HEALTH_PATTERN.matcher(requestPath).matches()
                || PUBLIC_PLAYLIST_LIST_PATTERN.matcher(requestPath).matches()
                || PUBLIC_PLAYLIST_RANKING_PATTERN.matcher(requestPath).matches()
                || PUBLIC_PLAYLIST_DETAIL_PATTERN.matcher(requestPath).matches()
                || PUBLIC_PLAYLIST_CHILD_PATTERN.matcher(requestPath).matches()
                || PUBLIC_SPOTIFY_TRACK_SEARCH_PATTERN.matcher(requestPath).matches();
    }

    private String getRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isBlank()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

}
