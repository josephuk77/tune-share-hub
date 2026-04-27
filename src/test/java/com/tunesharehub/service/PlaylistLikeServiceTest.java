package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tunesharehub.dto.PlaylistLikeResponse;
import com.tunesharehub.entity.PlaylistLike;
import com.tunesharehub.mapper.PlaylistLikeMapper;
import com.tunesharehub.mapper.PlaylistMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistLikeServiceTest {

    private static final long USER_ID = 1L;
    private static final long PLAYLIST_ID = 10L;

    @Mock
    private PlaylistService playlistService;

    @Mock
    private PlaylistMapper playlistMapper;

    @Mock
    private PlaylistLikeMapper playlistLikeMapper;

    @InjectMocks
    private PlaylistLikeService playlistLikeService;

    @Test
    void likePlaylistInsertsNewLikeAndIncreasesCount() {
        when(playlistLikeMapper.findByPlaylistIdAndUserId(PLAYLIST_ID, USER_ID)).thenReturn(null);
        when(playlistMapper.findLikeCountById(PLAYLIST_ID)).thenReturn(1L);

        PlaylistLikeResponse response = playlistLikeService.likePlaylist(USER_ID, PLAYLIST_ID);

        ArgumentCaptor<PlaylistLike> captor = ArgumentCaptor.forClass(PlaylistLike.class);
        verify(playlistService).validateReadableForUpdate(USER_ID, PLAYLIST_ID);
        verify(playlistLikeMapper).insert(captor.capture());
        verify(playlistMapper).increaseLikeCount(PLAYLIST_ID);
        assertThat(captor.getValue().getPlaylistId()).isEqualTo(PLAYLIST_ID);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(response).isEqualTo(new PlaylistLikeResponse(PLAYLIST_ID, true, 1L));
    }

    @Test
    void likePlaylistDoesNothingWhenAlreadyActive() {
        when(playlistLikeMapper.findByPlaylistIdAndUserId(PLAYLIST_ID, USER_ID)).thenReturn(activeLike());
        when(playlistMapper.findLikeCountById(PLAYLIST_ID)).thenReturn(1L);

        PlaylistLikeResponse response = playlistLikeService.likePlaylist(USER_ID, PLAYLIST_ID);

        verify(playlistLikeMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(playlistLikeMapper, never()).recover(PLAYLIST_ID, USER_ID);
        verify(playlistMapper, never()).increaseLikeCount(PLAYLIST_ID);
        assertThat(response).isEqualTo(new PlaylistLikeResponse(PLAYLIST_ID, true, 1L));
    }

    @Test
    void likePlaylistRecoversDeletedLikeAndIncreasesCount() {
        when(playlistLikeMapper.findByPlaylistIdAndUserId(PLAYLIST_ID, USER_ID)).thenReturn(deletedLike());
        when(playlistMapper.findLikeCountById(PLAYLIST_ID)).thenReturn(1L);

        PlaylistLikeResponse response = playlistLikeService.likePlaylist(USER_ID, PLAYLIST_ID);

        verify(playlistLikeMapper).recover(PLAYLIST_ID, USER_ID);
        verify(playlistMapper).increaseLikeCount(PLAYLIST_ID);
        assertThat(response).isEqualTo(new PlaylistLikeResponse(PLAYLIST_ID, true, 1L));
    }

    @Test
    void unlikePlaylistSoftDeletesActiveLikeAndDecreasesCount() {
        when(playlistLikeMapper.findByPlaylistIdAndUserId(PLAYLIST_ID, USER_ID)).thenReturn(activeLike());
        when(playlistMapper.findLikeCountById(PLAYLIST_ID)).thenReturn(0L);

        PlaylistLikeResponse response = playlistLikeService.unlikePlaylist(USER_ID, PLAYLIST_ID);

        verify(playlistLikeMapper).softDelete(PLAYLIST_ID, USER_ID);
        verify(playlistMapper).decreaseLikeCount(PLAYLIST_ID);
        assertThat(response).isEqualTo(new PlaylistLikeResponse(PLAYLIST_ID, false, 0L));
    }

    @Test
    void unlikePlaylistDoesNothingWhenLikeDoesNotExist() {
        when(playlistLikeMapper.findByPlaylistIdAndUserId(PLAYLIST_ID, USER_ID)).thenReturn(null);
        when(playlistMapper.findLikeCountById(PLAYLIST_ID)).thenReturn(0L);

        PlaylistLikeResponse response = playlistLikeService.unlikePlaylist(USER_ID, PLAYLIST_ID);

        verify(playlistLikeMapper, never()).softDelete(PLAYLIST_ID, USER_ID);
        verify(playlistMapper, never()).decreaseLikeCount(PLAYLIST_ID);
        assertThat(response).isEqualTo(new PlaylistLikeResponse(PLAYLIST_ID, false, 0L));
    }

    private PlaylistLike activeLike() {
        PlaylistLike playlistLike = new PlaylistLike();
        playlistLike.setLikeId(100L);
        playlistLike.setPlaylistId(PLAYLIST_ID);
        playlistLike.setUserId(USER_ID);
        return playlistLike;
    }

    private PlaylistLike deletedLike() {
        PlaylistLike playlistLike = activeLike();
        playlistLike.setDeletedAt(LocalDateTime.now());
        return playlistLike;
    }
}
