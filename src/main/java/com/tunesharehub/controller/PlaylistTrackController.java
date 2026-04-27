package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.PlaylistTrackCreateRequest;
import com.tunesharehub.dto.PlaylistTrackReorderRequest;
import com.tunesharehub.dto.PlaylistTrackResponse;
import com.tunesharehub.service.PlaylistTrackService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists/{playlistId}/tracks")
public class PlaylistTrackController {

    private final PlaylistTrackService playlistTrackService;

    public PlaylistTrackController(PlaylistTrackService playlistTrackService) {
        this.playlistTrackService = playlistTrackService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PlaylistTrackResponse addTrack(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistTrackCreateRequest request
    ) {
        return playlistTrackService.addTrack(authUser.userId(), playlistId, request);
    }

    @GetMapping
    public List<PlaylistTrackResponse> getTracks(
            @CurrentUser(required = false) AuthUser authUser,
            @PathVariable Long playlistId
    ) {
        return playlistTrackService.getTracks(AuthUser.userIdOrNull(authUser), playlistId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{playlistTrackId}")
    public void deleteTrack(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @PathVariable Long playlistTrackId
    ) {
        playlistTrackService.deleteTrack(authUser.userId(), playlistId, playlistTrackId);
    }

    @PutMapping("/positions")
    public List<PlaylistTrackResponse> reorderTracks(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistTrackReorderRequest request
    ) {
        return playlistTrackService.reorderTracks(authUser.userId(), playlistId, request);
    }
}
