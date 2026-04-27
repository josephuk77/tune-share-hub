package com.tunesharehub.dto;

import java.time.LocalDateTime;

public record PlaylistTrackResponse(
        Long playlistTrackId,
        Long playlistId,
        String spotifyTrackId,
        String title,
        String artistName,
        String albumName,
        String albumImageUrl,
        String spotifyUrl,
        String previewUrl,
        Long durationMs,
        Long positionNo,
        LocalDateTime createdAt
) {
}
