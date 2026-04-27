package com.tunesharehub.exception;

public class SpotifyApiException extends BusinessException {

    public SpotifyApiException(String message) {
        super("SPOTIFY_API_ERROR", message);
    }
}
