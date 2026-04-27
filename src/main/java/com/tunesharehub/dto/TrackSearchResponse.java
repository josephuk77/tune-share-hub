package com.tunesharehub.dto;

import java.util.List;

public record TrackSearchResponse(
        String query,
        int page,
        int size,
        int total,
        boolean hasNext,
        List<TrackSearchItemResponse> tracks
) {
}
