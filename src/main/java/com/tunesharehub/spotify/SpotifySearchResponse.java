package com.tunesharehub.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifySearchResponse(
        TrackPage tracks
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrackPage(
            String next,
            int total,
            List<SpotifyTrack> items
    ) {
    }
}
