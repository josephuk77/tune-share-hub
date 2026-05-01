package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tunesharehub.dto.TrackSearchResponse;
import com.tunesharehub.spotify.SpotifyClient;
import com.tunesharehub.spotify.SpotifySearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock
    private SpotifyClient spotifyClient;

    @Mock
    private SearchHistoryService searchHistoryService;

    @InjectMocks
    private SpotifyService spotifyService;

    @Test
    void searchTracksRecordsHistoryForLoginUser() {
        SpotifySearchResponse response = new SpotifySearchResponse(
                new SpotifySearchResponse.TrackPage(null, 0, List.of())
        );
        when(spotifyClient.searchTracks("iu", 10, 0)).thenReturn(response);

        TrackSearchResponse result = spotifyService.searchTracks(1L, "  iu  ", 0, 10);

        verify(spotifyClient).searchTracks("iu", 10, 0);
        verify(searchHistoryService).recordSearch(1L, "iu");
        assertThat(result.query()).isEqualTo("iu");
    }
}
