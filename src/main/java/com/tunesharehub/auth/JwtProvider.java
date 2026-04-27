package com.tunesharehub.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = Duration.ofMinutes(accessTokenExpirationMinutes);
        this.refreshTokenExpiration = Duration.ofDays(refreshTokenExpirationDays);
    }

    public String createAccessToken(AuthUser user) {
        return createToken(user, ACCESS_TOKEN_TYPE, accessTokenExpiration);
    }

    public String createRefreshToken(AuthUser user) {
        return createToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
    }

    public AuthUser parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, ACCESS_TOKEN_TYPE);
        return toAuthUser(claims);
    }

    public AuthUser parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        return toAuthUser(claims);
    }

    public Instant getRefreshTokenExpiresAt(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        return claims.getExpiration().toInstant();
    }

    private String createToken(AuthUser user, String tokenType, Duration expiration) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);

        return Jwts.builder()
                .subject(String.valueOf(user.userId()))
                .claim(CLAIM_USER_ID, user.userId())
                .claim(CLAIM_EMAIL, user.email())
                .claim(CLAIM_NICKNAME, user.nickname())
                .claim(CLAIM_ROLE, user.role())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!expectedTokenType.equals(tokenType)) {
            throw new InvalidTokenException("토큰 유형이 올바르지 않습니다.");
        }
    }

    private AuthUser toAuthUser(Claims claims) {
        return new AuthUser(
                getUserId(claims),
                claims.get(CLAIM_EMAIL, String.class),
                claims.get(CLAIM_NICKNAME, String.class),
                claims.get(CLAIM_ROLE, String.class)
        );
    }

    private Long getUserId(Claims claims) {
        Number userId = claims.get(CLAIM_USER_ID, Number.class);
        if (userId == null) {
            throw new InvalidTokenException("토큰 사용자 정보가 올바르지 않습니다.");
        }
        return userId.longValue();
    }
}
