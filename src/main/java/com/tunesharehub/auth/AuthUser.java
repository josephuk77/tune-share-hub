package com.tunesharehub.auth;

public record AuthUser(
        Long userId,
        String email,
        String nickname,
        String role
) {
}
