package com.tunesharehub.service;

import com.tunesharehub.dto.TrackSearchItemResponse;
import com.tunesharehub.dto.TrackSearchResponse;
import com.tunesharehub.exception.SpotifyApiException;
import com.tunesharehub.spotify.SpotifyClient;
import com.tunesharehub.spotify.SpotifySearchResponse;
import com.tunesharehub.spotify.SpotifyTrack;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    private static final Logger log = LoggerFactory.getLogger(SpotifyService.class);

    private final SpotifyClient spotifyClient;
    private final SearchHistoryService searchHistoryService;

    public SpotifyService(SpotifyClient spotifyClient, SearchHistoryService searchHistoryService) {
        this.spotifyClient = spotifyClient;
        this.searchHistoryService = searchHistoryService;
    }

    public TrackSearchResponse searchTracks(Long userId, String query, int page, int size) {
        String normalizedQuery = query.trim();
        int offset = page * size;
        if (offset > 1000) {
            throw new SpotifyApiException("Spotify 검색 offset은 1000을 초과할 수 없습니다.");
        }

        SpotifySearchResponse response = spotifyClient.searchTracks(normalizedQuery, size, offset);
        if (response == null) {
            throw new SpotifyApiException("Spotify 트랙 검색 응답이 올바르지 않습니다.");
        }

        SpotifySearchResponse.TrackPage tracks = response.tracks();
        if (tracks == null || tracks.items() == null) {
            throw new SpotifyApiException("Spotify 트랙 검색 응답이 올바르지 않습니다.");
        }

        List<TrackSearchItemResponse> items = tracks.items()
                .stream()
                .map(this::toTrackSearchItemResponse)
                .toList();

        recordSearchHistory(userId, normalizedQuery);

        return new TrackSearchResponse(
                normalizedQuery,
                page,
                size,
                tracks.total(),
                tracks.next() != null,
                items
        );
    }

    private void recordSearchHistory(Long userId, String normalizedQuery) {
        try {
            searchHistoryService.recordSearch(userId, normalizedQuery);
        } catch (RuntimeException exception) {
            log.warn("Failed to record Spotify search history. userId={}, query={}", userId, normalizedQuery, exception);
        }
    }

    private TrackSearchItemResponse toTrackSearchItemResponse(SpotifyTrack track) {
        return new TrackSearchItemResponse(
                track.id(),
                track.name(),
                track.artistName(),
                track.albumName(),
                track.albumImageUrl(),
                track.spotifyUrl(),
                track.previewUrl(),
                track.durationMs()
        );
    }
}
