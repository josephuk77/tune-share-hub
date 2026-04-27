package com.tunesharehub.service;

import com.tunesharehub.dto.PlaylistLikeResponse;
import com.tunesharehub.entity.PlaylistLike;
import com.tunesharehub.mapper.PlaylistLikeMapper;
import com.tunesharehub.mapper.PlaylistMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistLikeService {

    private final PlaylistService playlistService;
    private final PlaylistMapper playlistMapper;
    private final PlaylistLikeMapper playlistLikeMapper;

    public PlaylistLikeService(
            PlaylistService playlistService,
            PlaylistMapper playlistMapper,
            PlaylistLikeMapper playlistLikeMapper
    ) {
        this.playlistService = playlistService;
        this.playlistMapper = playlistMapper;
        this.playlistLikeMapper = playlistLikeMapper;
    }

    @Transactional
    public PlaylistLikeResponse likePlaylist(Long userId, Long playlistId) {
        playlistService.validateReadableForUpdate(userId, playlistId);

        PlaylistLike playlistLike = playlistLikeMapper.findByPlaylistIdAndUserId(playlistId, userId);
        if (playlistLike == null) {
            insertLike(userId, playlistId);
            playlistMapper.increaseLikeCount(playlistId);
            return toResponse(playlistId, true);
        }

        if (playlistLike.getDeletedAt() == null) {
            return toResponse(playlistId, true);
        }

        playlistLikeMapper.recover(playlistId, userId);
        playlistMapper.increaseLikeCount(playlistId);
        return toResponse(playlistId, true);
    }

    @Transactional
    public PlaylistLikeResponse unlikePlaylist(Long userId, Long playlistId) {
        playlistService.validateReadableForUpdate(userId, playlistId);

        PlaylistLike playlistLike = playlistLikeMapper.findByPlaylistIdAndUserId(playlistId, userId);
        if (playlistLike == null || playlistLike.getDeletedAt() != null) {
            return toResponse(playlistId, false);
        }

        playlistLikeMapper.softDelete(playlistId, userId);
        playlistMapper.decreaseLikeCount(playlistId);
        return toResponse(playlistId, false);
    }

    private void insertLike(Long userId, Long playlistId) {
        PlaylistLike playlistLike = new PlaylistLike();
        playlistLike.setUserId(userId);
        playlistLike.setPlaylistId(playlistId);
        playlistLikeMapper.insert(playlistLike);
    }

    private PlaylistLikeResponse toResponse(Long playlistId, boolean liked) {
        return new PlaylistLikeResponse(
                playlistId,
                liked,
                playlistMapper.findLikeCountById(playlistId)
        );
    }
}
