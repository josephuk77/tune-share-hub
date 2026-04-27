package com.tunesharehub.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserSummaryResponse user
) {
}
