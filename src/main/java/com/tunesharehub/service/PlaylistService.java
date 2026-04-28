package com.tunesharehub.service;

import com.tunesharehub.dto.PlaylistCreateRequest;
import com.tunesharehub.dto.PlaylistResponse;
import com.tunesharehub.dto.PlaylistUpdateRequest;
import com.tunesharehub.entity.Playlist;
import com.tunesharehub.exception.BusinessException;
import com.tunesharehub.exception.ForbiddenException;
import com.tunesharehub.exception.PlaylistNotFoundException;
import com.tunesharehub.mapper.PlaylistMapper;
import com.tunesharehub.mapper.PlaylistTrackMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistService {

    private static final String PUBLIC_YN_TRUE = "Y";
    private static final String PUBLIC_YN_FALSE = "N";
    private static final String SEARCH_TYPE_TITLE = "title";
    private static final String SEARCH_TYPE_AUTHOR = "author";
    private static final String SORT_LATEST = "latest";
    private static final String SORT_VIEW = "view";
    private static final String SORT_LIKE = "like";
    private static final String SORT_COMMENT = "comment";

    private final PlaylistMapper playlistMapper;
    private final PlaylistTrackMapper playlistTrackMapper;

    public PlaylistService(PlaylistMapper playlistMapper, PlaylistTrackMapper playlistTrackMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistTrackMapper = playlistTrackMapper;
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

    @Transactional
    public PlaylistResponse getPlaylistDetail(Long loginUserId, Long playlistId) {
        Playlist playlist = getExistingPlaylist(playlistId);
        validateReadable(loginUserId, playlist);
        if (shouldIncreaseViewCount(loginUserId, playlist)) {
            int updatedCount = playlistMapper.increaseViewCount(playlistId);
            if (updatedCount == 1) {
                long currentViewCount = playlist.getViewCount() != null ? playlist.getViewCount() : 0L;
                playlist.setViewCount(currentViewCount + 1);
            }
        }
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

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getLikedPlaylists(Long userId, int page, int size) {
        int offset = page * size;
        return playlistMapper.findLikedByUserId(userId, offset, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> getPublicPlaylists(
            String keyword,
            String searchType,
            String sort,
            int page,
            int size
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSearchType = normalizeSearchType(searchType);
        String normalizedSort = normalizeSort(sort);
        int offset = page * size;

        return playlistMapper.findPublicPlaylists(
                        normalizedKeyword,
                        normalizedSearchType,
                        normalizedSort,
                        offset,
                        size
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PlaylistResponse copyPlaylist(Long userId, Long sourcePlaylistId) {
        Playlist sourcePlaylist = playlistMapper.findById(sourcePlaylistId);
        if (sourcePlaylist == null) {
            throw new PlaylistNotFoundException();
        }
        validateReadable(userId, sourcePlaylist);

        Playlist copiedPlaylist = new Playlist();
        copiedPlaylist.setUserId(userId);
        copiedPlaylist.setTitle(sourcePlaylist.getTitle());
        copiedPlaylist.setDescription(sourcePlaylist.getDescription());
        copiedPlaylist.setCoverImageUrl(sourcePlaylist.getCoverImageUrl());
        copiedPlaylist.setPublicYn(PUBLIC_YN_FALSE);
        copiedPlaylist.setOriginPlaylistId(sourcePlaylist.getPlaylistId());
        copiedPlaylist.setOriginUserNickname(sourcePlaylist.getUserNickname());

        playlistMapper.insert(copiedPlaylist);
        playlistTrackMapper.copyActiveTracks(sourcePlaylistId, copiedPlaylist.getPlaylistId());
        int updatedCount = playlistMapper.increaseCopyCount(sourcePlaylistId);
        if (updatedCount != 1) {
            throw new PlaylistNotFoundException();
        }

        return getPlaylist(userId, copiedPlaylist.getPlaylistId());
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
                playlist.getOriginPlaylistId(),
                playlist.getOriginUserNickname(),
                playlist.getViewCount(),
                playlist.getLikeCount(),
                playlist.getCommentCount(),
                playlist.getCopyCount(),
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    private boolean shouldIncreaseViewCount(Long loginUserId, Playlist playlist) {
        return PUBLIC_YN_TRUE.equals(playlist.getPublicYn()) && !playlist.getUserId().equals(loginUserId);
    }

    private String normalizeSearchType(String searchType) {
        if (searchType == null || searchType.isBlank()) {
            return SEARCH_TYPE_TITLE;
        }

        String normalizedSearchType = searchType.trim().toLowerCase(Locale.ROOT);
        if (SEARCH_TYPE_AUTHOR.equals(normalizedSearchType)) {
            return SEARCH_TYPE_AUTHOR;
        }
        if (SEARCH_TYPE_TITLE.equals(normalizedSearchType)) {
            return SEARCH_TYPE_TITLE;
        }
        throw new BusinessException("INVALID_SEARCH_TYPE", "검색 타입이 올바르지 않습니다.");
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return SORT_LATEST;
        }

        String normalizedSort = sort.trim().toLowerCase(Locale.ROOT);
        if (SORT_LATEST.equals(normalizedSort)) {
            return SORT_LATEST;
        }
        if (SORT_VIEW.equals(normalizedSort) || SORT_LIKE.equals(normalizedSort)
                || SORT_COMMENT.equals(normalizedSort)) {
            return normalizedSort;
        }
        throw new BusinessException("INVALID_SORT", "정렬 값이 올바르지 않습니다.");
    }
}
