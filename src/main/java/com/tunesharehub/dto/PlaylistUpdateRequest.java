package com.tunesharehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String title,

        String description,

        @Size(max = 1000)
        String coverImageUrl,

        Boolean publicYn
) {
}
