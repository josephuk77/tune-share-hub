package com.tunesharehub.auth;

public record AuthUser(
        Long userId,
        String email,
        String nickname,
        String role
) {

    public static Long userIdOrNull(AuthUser authUser) {
        if (authUser == null) {
            return null;
        }
        return authUser.userId();
    }
}
