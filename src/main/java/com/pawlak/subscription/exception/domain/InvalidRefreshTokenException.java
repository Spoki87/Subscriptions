package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token", null);
    }
}
