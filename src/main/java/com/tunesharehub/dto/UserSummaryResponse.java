package com.tunesharehub.dto;

public record UserSummaryResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {
}
