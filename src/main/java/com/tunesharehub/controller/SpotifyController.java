package com.tunesharehub.controller;

import com.tunesharehub.dto.TrackSearchResponse;
import com.tunesharehub.service.SpotifyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search/tracks")
    public TrackSearchResponse searchTracks(
            @RequestParam @NotBlank String q,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(10) int size
    ) {
        return spotifyService.searchTracks(q, page, size);
    }
}
