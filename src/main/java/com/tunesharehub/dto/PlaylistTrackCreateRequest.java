package com.tunesharehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistTrackCreateRequest(
        @NotBlank
        @Size(max = 100)
        String spotifyTrackId,

        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 255)
        String artistName,

        @Size(max = 255)
        String albumName,

        @Size(max = 1000)
        String albumImageUrl,

        @NotBlank
        @Size(max = 1000)
        String spotifyUrl,

        @Size(max = 1000)
        String previewUrl,

        Long durationMs
) {
}
