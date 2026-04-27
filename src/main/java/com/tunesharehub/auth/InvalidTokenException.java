package com.tunesharehub.auth;

import com.tunesharehub.exception.BusinessException;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}
