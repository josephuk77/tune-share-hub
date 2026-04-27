package com.tunesharehub.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyTrack(
        String id,
        String name,
        List<SpotifyArtist> artists,
        SpotifyAlbum album,
        @JsonProperty("external_urls")
        Map<String, String> externalUrls,
        @JsonProperty("preview_url")
        String previewUrl,
        @JsonProperty("duration_ms")
        Long durationMs
) {

    public String artistName() {
        if (artists == null || artists.isEmpty()) {
            return "";
        }
        return String.join(", ", artists.stream().map(SpotifyArtist::name).toList());
    }

    public String albumName() {
        if (album == null) {
            return null;
        }
        return album.name();
    }

    public String albumImageUrl() {
        if (album == null || album.images() == null || album.images().isEmpty()) {
            return null;
        }
        return album.images().get(0).url();
    }

    public String spotifyUrl() {
        if (externalUrls == null) {
            return null;
        }
        return externalUrls.get("spotify");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpotifyArtist(
            String name
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpotifyAlbum(
            String name,
            List<SpotifyImage> images
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpotifyImage(
            String url
    ) {
    }
}
