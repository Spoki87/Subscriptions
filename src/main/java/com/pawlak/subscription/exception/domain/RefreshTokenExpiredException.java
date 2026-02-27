package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;

public class RefreshTokenExpiredException extends BusinessException {
    public RefreshTokenExpiredException() {
        super("Refresh token expired", null);
    }
}
