package com.tunesharehub.auth;

import com.tunesharehub.exception.BusinessException;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
