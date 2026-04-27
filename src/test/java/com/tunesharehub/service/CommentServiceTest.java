package com.tunesharehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tunesharehub.dto.MyCommentResponse;
import com.tunesharehub.entity.Comment;
import com.tunesharehub.mapper.CommentMapper;
import com.tunesharehub.mapper.PlaylistMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PlaylistService playlistService;

    @Mock
    private PlaylistMapper playlistMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getMyCommentsReturnsUserCommentsWithPlaylistContext() {
        Comment comment = new Comment();
        comment.setCommentId(100L);
        comment.setPlaylistId(10L);
        comment.setPlaylistTitle("morning mix");
        comment.setContent("좋아요");
        comment.setCreatedAt(LocalDateTime.of(2026, 4, 27, 10, 0));
        comment.setUpdatedAt(LocalDateTime.of(2026, 4, 27, 11, 0));
        when(commentMapper.findByUserId(1L, 20, 10)).thenReturn(List.of(comment));

        List<MyCommentResponse> responses = commentService.getMyComments(1L, 2, 10);

        verify(commentMapper).findByUserId(1L, 20, 10);
        assertThat(responses).containsExactly(new MyCommentResponse(
                100L,
                10L,
                "morning mix",
                "좋아요",
                LocalDateTime.of(2026, 4, 27, 10, 0),
                LocalDateTime.of(2026, 4, 27, 11, 0)
        ));
    }
}
