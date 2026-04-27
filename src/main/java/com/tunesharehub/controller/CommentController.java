package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.CommentCreateRequest;
import com.tunesharehub.dto.CommentResponse;
import com.tunesharehub.dto.CommentUpdateRequest;
import com.tunesharehub.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class CommentController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/playlists/{playlistId}/comments")
    public CommentResponse createComment(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return commentService.createComment(authUser.userId(), playlistId, request);
    }

    @GetMapping("/playlists/{playlistId}/comments")
    public List<CommentResponse> getComments(
            @CurrentUser AuthUser authUser,
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(100) int size
    ) {
        return commentService.getComments(authUser.userId(), playlistId, page, size);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @CurrentUser AuthUser authUser,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return commentService.updateComment(authUser.userId(), commentId, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @CurrentUser AuthUser authUser,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(authUser.userId(), commentId);
    }
}
