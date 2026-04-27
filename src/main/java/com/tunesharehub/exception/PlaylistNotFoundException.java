package com.tunesharehub.exception;

public class PlaylistNotFoundException extends BusinessException {

    public PlaylistNotFoundException() {
        super("PLAYLIST_NOT_FOUND", "플레이리스트를 찾을 수 없습니다.");
    }
}
