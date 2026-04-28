package com.tunesharehub.dto;

import java.time.LocalDateTime;

public record MyCommentResponse(
        Long commentId,
        Long playlistId,
        String playlistTitle,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
