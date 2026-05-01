package com.tunesharehub.dto;

import java.time.LocalDateTime;

public record SearchHistoryResponse(
        Long searchHistoryId,
        String query,
        LocalDateTime createdAt
) {
}
