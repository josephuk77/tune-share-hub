package com.tunesharehub.service;

import com.tunesharehub.dto.PlaylistLikeResponse;
import com.tunesharehub.entity.PlaylistLike;
import com.tunesharehub.mapper.PlaylistLikeMapper;
import com.tunesharehub.mapper.PlaylistMapper;
import org.springframework.dao.DuplicateKeyException;
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
        playlistService.validateReadable(userId, playlistId);

        PlaylistLike playlistLike = playlistLikeMapper.findByPlaylistIdAndUserId(playlistId, userId);
        if (playlistLike == null) {
            if (insertLike(userId, playlistId)) {
                playlistMapper.increaseLikeCount(playlistId);
            }
            return toResponse(playlistId, true);
        }

        if (playlistLike.getDeletedAt() == null) {
            return toResponse(playlistId, true);
        }

        int recoveredCount = playlistLikeMapper.recover(playlistId, userId);
        if (recoveredCount == 1) {
            playlistMapper.increaseLikeCount(playlistId);
        }
        return toResponse(playlistId, true);
    }

    @Transactional
    public PlaylistLikeResponse unlikePlaylist(Long userId, Long playlistId) {
        playlistService.validateReadable(userId, playlistId);

        PlaylistLike playlistLike = playlistLikeMapper.findByPlaylistIdAndUserId(playlistId, userId);
        if (playlistLike == null || playlistLike.getDeletedAt() != null) {
            return toResponse(playlistId, false);
        }

        int deletedCount = playlistLikeMapper.softDelete(playlistId, userId);
        if (deletedCount == 1) {
            playlistMapper.decreaseLikeCount(playlistId);
        }
        return toResponse(playlistId, false);
    }

    private boolean insertLike(Long userId, Long playlistId) {
        PlaylistLike playlistLike = new PlaylistLike();
        playlistLike.setUserId(userId);
        playlistLike.setPlaylistId(playlistId);
        try {
            playlistLikeMapper.insert(playlistLike);
            return true;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }

    private PlaylistLikeResponse toResponse(Long playlistId, boolean liked) {
        return new PlaylistLikeResponse(
                playlistId,
                liked,
                playlistMapper.findLikeCountById(playlistId)
        );
    }
}
