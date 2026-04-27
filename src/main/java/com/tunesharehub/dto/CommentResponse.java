package com.tunesharehub.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long playlistId,
        Long userId,
        String userNickname,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
