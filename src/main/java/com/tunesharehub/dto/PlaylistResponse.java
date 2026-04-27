package com.tunesharehub.dto;

import java.time.LocalDateTime;

public record PlaylistResponse(
        Long playlistId,
        Long userId,
        String userNickname,
        String title,
        String description,
        String coverImageUrl,
        Boolean publicYn,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
