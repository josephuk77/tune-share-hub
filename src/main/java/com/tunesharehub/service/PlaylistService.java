package com.tunesharehub.service;

import com.tunesharehub.dto.PlaylistCreateRequest;
import com.tunesharehub.dto.PlaylistResponse;
import com.tunesharehub.dto.PlaylistUpdateRequest;
import com.tunesharehub.entity.Playlist;
import com.tunesharehub.exception.ForbiddenException;
import com.tunesharehub.exception.PlaylistNotFoundException;
import com.tunesharehub.mapper.PlaylistMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistService {

    private static final String PUBLIC_YN_TRUE = "Y";
    private static final String PUBLIC_YN_FALSE = "N";

    private final PlaylistMapper playlistMapper;

    public PlaylistService(PlaylistMapper playlistMapper) {
        this.playlistMapper = playlistMapper;
    }

    @Transactional
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request) {
        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setTitle(request.title());
        playlist.setDescription(request.description());
        playlist.setCoverImageUrl(request.coverImageUrl());
        playlist.setPublicYn(toYn(request.publicYn()));

        playlistMapper.insert(playlist);
        return getPlaylist(userId, playlist.getPlaylistId());
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylist(Long loginUserId, Long playlistId) {
        Playlist playlist = getExistingPlaylist(playlistId);
        validateReadable(loginUserId, playlist);
        return toResponse(playlist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylists(Long userId, int page, int size) {
        int offset = page * size;
        return playlistMapper.findByUserId(userId, offset, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long userId, Long playlistId, PlaylistUpdateRequest request) {
        Playlist playlist = getExistingPlaylist(playlistId);
        validateOwner(userId, playlist);

        playlist.setTitle(request.title());
        playlist.setDescription(request.description());
        playlist.setCoverImageUrl(request.coverImageUrl());
        if (request.publicYn() != null) {
            playlist.setPublicYn(toYn(request.publicYn()));
        }

        int updatedCount = playlistMapper.updateOwned(playlist);
        if (updatedCount != 1) {
            throw new PlaylistNotFoundException();
        }

        return getPlaylist(userId, playlistId);
    }

    @Transactional
    public void deletePlaylist(Long userId, Long playlistId) {
        Playlist playlist = getExistingPlaylist(playlistId);
        validateOwner(userId, playlist);

        int deletedCount = playlistMapper.softDeleteOwned(playlistId, userId);
        if (deletedCount != 1) {
            throw new PlaylistNotFoundException();
        }

        playlistMapper.softDeleteTracksByPlaylistId(playlistId);
        playlistMapper.softDeleteCommentsByPlaylistId(playlistId);
        playlistMapper.softDeleteLikesByPlaylistId(playlistId);
    }

    private Playlist getExistingPlaylist(Long playlistId) {
        Playlist playlist = playlistMapper.findById(playlistId);
        if (playlist == null) {
            throw new PlaylistNotFoundException();
        }
        return playlist;
    }

    private void validateReadable(Long loginUserId, Playlist playlist) {
        if (PUBLIC_YN_TRUE.equals(playlist.getPublicYn()) || playlist.getUserId().equals(loginUserId)) {
            return;
        }
        throw new ForbiddenException("비공개 플레이리스트에 접근할 수 없습니다.");
    }

    private void validateOwner(Long userId, Playlist playlist) {
        if (!playlist.getUserId().equals(userId)) {
            throw new ForbiddenException("플레이리스트 작성자만 변경할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void validateOwner(Long userId, Long playlistId) {
        Playlist playlist = getExistingPlaylist(playlistId);
        validateOwner(userId, playlist);
    }

    @Transactional(readOnly = true)
    public void validateReadable(Long userId, Long playlistId) {
        Playlist playlist = playlistMapper.findAccessById(playlistId);
        if (playlist == null) {
            throw new PlaylistNotFoundException();
        }
        validateReadable(userId, playlist);
    }

    @Transactional
    public void validateOwnerForUpdate(Long userId, Long playlistId) {
        Playlist playlist = playlistMapper.findByIdForUpdate(playlistId);
        if (playlist == null) {
            throw new PlaylistNotFoundException();
        }
        validateOwner(userId, playlist);
    }

    @Transactional
    public void validateReadableForUpdate(Long userId, Long playlistId) {
        Playlist playlist = playlistMapper.findByIdForUpdate(playlistId);
        if (playlist == null) {
            throw new PlaylistNotFoundException();
        }
        validateReadable(userId, playlist);
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getPlaylistId(),
                playlist.getUserId(),
                playlist.getUserNickname(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getCoverImageUrl(),
                PUBLIC_YN_TRUE.equals(playlist.getPublicYn()),
                playlist.getViewCount(),
                playlist.getLikeCount(),
                playlist.getCommentCount(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }

    private String toYn(Boolean value) {
        if (value == null || value) {
            return PUBLIC_YN_TRUE;
        }
        return PUBLIC_YN_FALSE;
    }
}
