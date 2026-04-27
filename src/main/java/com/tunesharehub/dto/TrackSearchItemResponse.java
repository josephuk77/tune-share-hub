package com.tunesharehub.dto;

public record TrackSearchItemResponse(
        String spotifyTrackId,
        String title,
        String artistName,
        String albumName,
        String albumImageUrl,
        String spotifyUrl,
        String previewUrl,
        Long durationMs
) {
}
