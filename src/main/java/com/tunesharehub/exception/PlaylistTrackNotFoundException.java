package com.tunesharehub.exception;

public class PlaylistTrackNotFoundException extends BusinessException {

    public PlaylistTrackNotFoundException() {
        super("PLAYLIST_TRACK_NOT_FOUND", "플레이리스트 트랙을 찾을 수 없습니다.");
    }
}
