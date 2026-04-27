package com.tunesharehub.service;

import com.tunesharehub.dto.PlaylistTrackCreateRequest;
import com.tunesharehub.dto.PlaylistTrackReorderRequest;
import com.tunesharehub.dto.PlaylistTrackResponse;
import com.tunesharehub.entity.PlaylistTrack;
import com.tunesharehub.exception.PlaylistTrackNotFoundException;
import com.tunesharehub.mapper.PlaylistTrackMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistTrackService {

    private final PlaylistService playlistService;
    private final PlaylistTrackMapper playlistTrackMapper;

    public PlaylistTrackService(PlaylistService playlistService, PlaylistTrackMapper playlistTrackMapper) {
        this.playlistService = playlistService;
        this.playlistTrackMapper = playlistTrackMapper;
    }

    @Transactional
    public PlaylistTrackResponse addTrack(Long userId, Long playlistId, PlaylistTrackCreateRequest request) {
        playlistService.validateOwnerForUpdate(userId, playlistId);

        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylistId(playlistId);
        playlistTrack.setSpotifyTrackId(request.spotifyTrackId());
        playlistTrack.setTitle(request.title());
        playlistTrack.setArtistName(request.artistName());
        playlistTrack.setAlbumName(request.albumName());
        playlistTrack.setAlbumImageUrl(request.albumImageUrl());
        playlistTrack.setSpotifyUrl(request.spotifyUrl());
        playlistTrack.setPreviewUrl(request.previewUrl());
        playlistTrack.setDurationMs(request.durationMs());

        playlistTrackMapper.insert(playlistTrack);
        return toResponse(getExistingTrack(playlistId, playlistTrack.getPlaylistTrackId()));
    }

    @Transactional(readOnly = true)
    public List<PlaylistTrackResponse> getTracks(Long userId, Long playlistId) {
        playlistService.getPlaylist(userId, playlistId);
        return getTrackResponses(playlistId);
    }

    @Transactional
    public void deleteTrack(Long userId, Long playlistId, Long playlistTrackId) {
        playlistService.validateOwnerForUpdate(userId, playlistId);

        int deletedCount = playlistTrackMapper.softDelete(playlistId, playlistTrackId);
        if (deletedCount != 1) {
            throw new PlaylistTrackNotFoundException();
        }
    }

    @Transactional
    public List<PlaylistTrackResponse> reorderTracks(
            Long userId,
            Long playlistId,
            PlaylistTrackReorderRequest request
    ) {
        playlistService.validateOwnerForUpdate(userId, playlistId);
        validateAllTracksBelongToPlaylist(playlistId, request.playlistTrackIds());

        playlistTrackMapper.moveActiveTracksToTemporaryPositions(playlistId);
        playlistTrackMapper.updatePositions(playlistId, request.playlistTrackIds());

        return getTrackResponses(playlistId);
    }

    private PlaylistTrack getExistingTrack(Long playlistId, Long playlistTrackId) {
        PlaylistTrack playlistTrack = playlistTrackMapper.findById(playlistId, playlistTrackId);
        if (playlistTrack == null) {
            throw new PlaylistTrackNotFoundException();
        }
        return playlistTrack;
    }

    private void validateAllTracksBelongToPlaylist(Long playlistId, List<Long> playlistTrackIds) {
        int totalActiveTrackCount = playlistTrackMapper.countActiveByPlaylistId(playlistId);
        if (totalActiveTrackCount != playlistTrackIds.size()) {
            throw new PlaylistTrackNotFoundException();
        }

        int activeTrackCount = playlistTrackMapper.countActiveByIds(playlistId, playlistTrackIds);
        if (activeTrackCount != playlistTrackIds.size()) {
            throw new PlaylistTrackNotFoundException();
        }
    }

    private List<PlaylistTrackResponse> getTrackResponses(Long playlistId) {
        return playlistTrackMapper.findByPlaylistId(playlistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PlaylistTrackResponse toResponse(PlaylistTrack playlistTrack) {
        return new PlaylistTrackResponse(
                playlistTrack.getPlaylistTrackId(),
                playlistTrack.getPlaylistId(),
                playlistTrack.getSpotifyTrackId(),
                playlistTrack.getTitle(),
                playlistTrack.getArtistName(),
                playlistTrack.getAlbumName(),
                playlistTrack.getAlbumImageUrl(),
                playlistTrack.getSpotifyUrl(),
                playlistTrack.getPreviewUrl(),
                playlistTrack.getDurationMs(),
                playlistTrack.getPositionNo(),
                playlistTrack.getCreatedAt()
        );
    }
}
