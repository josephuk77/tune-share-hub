package com.tunesharehub.service;

import com.tunesharehub.dto.CommentCreateRequest;
import com.tunesharehub.dto.CommentResponse;
import com.tunesharehub.dto.CommentUpdateRequest;
import com.tunesharehub.entity.Comment;
import com.tunesharehub.exception.CommentNotFoundException;
import com.tunesharehub.exception.ForbiddenException;
import com.tunesharehub.mapper.CommentMapper;
import com.tunesharehub.mapper.PlaylistMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final PlaylistService playlistService;
    private final PlaylistMapper playlistMapper;
    private final CommentMapper commentMapper;

    public CommentService(
            PlaylistService playlistService,
            PlaylistMapper playlistMapper,
            CommentMapper commentMapper
    ) {
        this.playlistService = playlistService;
        this.playlistMapper = playlistMapper;
        this.commentMapper = commentMapper;
    }

    @Transactional
    public CommentResponse createComment(Long userId, Long playlistId, CommentCreateRequest request) {
        playlistService.validateReadableForUpdate(userId, playlistId);

        Comment comment = new Comment();
        comment.setPlaylistId(playlistId);
        comment.setUserId(userId);
        comment.setContent(request.content());

        commentMapper.insert(comment);
        playlistMapper.increaseCommentCount(playlistId);
        return toResponse(getExistingComment(comment.getCommentId()));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long userId, Long playlistId, int page, int size) {
        playlistService.getPlaylist(userId, playlistId);
        int offset = page * size;
        return commentMapper.findByPlaylistId(playlistId, offset, size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        Comment comment = getExistingComment(commentId);
        validateOwner(userId, comment);

        comment.setContent(request.content());
        int updatedCount = commentMapper.updateOwned(comment);
        if (updatedCount != 1) {
            throw new CommentNotFoundException();
        }

        return toResponse(getExistingComment(commentId));
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getExistingCommentForUpdate(commentId);
        validateOwner(userId, comment);

        int deletedCount = commentMapper.softDeleteOwned(commentId, userId);
        if (deletedCount != 1) {
            throw new CommentNotFoundException();
        }

        playlistMapper.decreaseCommentCount(comment.getPlaylistId());
    }

    private Comment getExistingComment(Long commentId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new CommentNotFoundException();
        }
        return comment;
    }

    private Comment getExistingCommentForUpdate(Long commentId) {
        Comment comment = commentMapper.findByIdForUpdate(commentId);
        if (comment == null) {
            throw new CommentNotFoundException();
        }
        return comment;
    }

    private void validateOwner(Long userId, Comment comment) {
        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("댓글 작성자만 변경할 수 있습니다.");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getPlaylistId(),
                comment.getUserId(),
                comment.getUserNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
