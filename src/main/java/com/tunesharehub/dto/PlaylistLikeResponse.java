package com.tunesharehub.dto;

public record PlaylistLikeResponse(
        Long playlistId,
        Boolean liked,
        Long likeCount
) {
}
