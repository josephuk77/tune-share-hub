package com.tunesharehub.exception;

public class CommentNotFoundException extends BusinessException {

    public CommentNotFoundException() {
        super("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.");
    }
}
