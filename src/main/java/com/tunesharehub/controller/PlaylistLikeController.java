package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.PlaylistLikeResponse;
import com.tunesharehub.service.PlaylistLikeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists/{playlistId}/likes")
public class PlaylistLikeController {

    private final PlaylistLikeService playlistLikeService;

    public PlaylistLikeController(PlaylistLikeService playlistLikeService) {
        this.playlistLikeService = playlistLikeService;
    }

    @PostMapping
    public PlaylistLikeResponse likePlaylist(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId
    ) {
        return playlistLikeService.likePlaylist(authUser.userId(), playlistId);
    }

    @DeleteMapping
    public PlaylistLikeResponse unlikePlaylist(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId
    ) {
        return playlistLikeService.unlikePlaylist(authUser.userId(), playlistId);
    }
}
