package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tunesharehub.dto.PlaylistResponse;
import com.tunesharehub.entity.Playlist;
import com.tunesharehub.exception.BusinessException;
import com.tunesharehub.mapper.PlaylistMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistMapper playlistMapper;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void getPublicPlaylistsNormalizesBlankParameters() {
        Playlist playlist = publicPlaylist();
        when(playlistMapper.findPublicPlaylists("", "title", "latest", 0, 20))
                .thenReturn(List.of(playlist));

        List<PlaylistResponse> responses = playlistService.getPublicPlaylists("  ", "", "", 0, 20);

        verify(playlistMapper).findPublicPlaylists("", "title", "latest", 0, 20);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).playlistId()).isEqualTo(10L);
    }

    @Test
    void getPublicPlaylistsAcceptsAuthorSearchAndLikeSort() {
        when(playlistMapper.findPublicPlaylists("alice", "author", "like", 40, 20))
                .thenReturn(List.of());

        playlistService.getPublicPlaylists(" alice ", " Author ", " LIKE ", 2, 20);

        verify(playlistMapper).findPublicPlaylists("alice", "author", "like", 40, 20);
    }

    @Test
    void getPublicPlaylistsRejectsInvalidSearchType() {
        assertThatThrownBy(() -> playlistService.getPublicPlaylists("", "description", "latest", 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasMessage("검색 타입이 올바르지 않습니다.");
    }

    @Test
    void getPublicPlaylistsRejectsInvalidSort() {
        assertThatThrownBy(() -> playlistService.getPublicPlaylists("", "title", "popular", 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasMessage("정렬 값이 올바르지 않습니다.");
    }

    private Playlist publicPlaylist() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(10L);
        playlist.setUserId(1L);
        playlist.setUserNickname("alice");
        playlist.setTitle("morning mix");
        playlist.setPublicYn("Y");
        playlist.setViewCount(0L);
        playlist.setLikeCount(0L);
        playlist.setCommentCount(0L);
        return playlist;
    }
}
