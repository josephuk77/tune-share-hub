package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.PlaylistCreateRequest;
import com.tunesharehub.dto.PlaylistResponse;
import com.tunesharehub.dto.PlaylistUpdateRequest;
import com.tunesharehub.service.PlaylistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class PlaylistController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/playlists")
    public PlaylistResponse createPlaylist(
            @CurrentUser AuthUser authUser,
            @Valid @RequestBody PlaylistCreateRequest request
    ) {
        return playlistService.createPlaylist(authUser.userId(), request);
    }

    @GetMapping("/playlists/{playlistId}")
    public PlaylistResponse getPlaylist(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId
    ) {
        return playlistService.getPlaylist(authUser.userId(), playlistId);
    }

    @GetMapping("/playlists")
    public List<PlaylistResponse> getPublicPlaylists(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "title") String searchType,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(100) int size
    ) {
        return playlistService.getPublicPlaylists(keyword, searchType, sort, page, size);
    }

    @GetMapping("/me/playlists")
    public List<PlaylistResponse> getMyPlaylists(
            @CurrentUser AuthUser authUser,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(100) int size
    ) {
        return playlistService.getMyPlaylists(authUser.userId(), page, size);
    }

    @PutMapping("/playlists/{playlistId}")
    public PlaylistResponse updatePlaylist(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistUpdateRequest request
    ) {
        return playlistService.updatePlaylist(authUser.userId(), playlistId, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/playlists/{playlistId}")
    public void deletePlaylist(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId
    ) {
        playlistService.deletePlaylist(authUser.userId(), playlistId);
    }
}
