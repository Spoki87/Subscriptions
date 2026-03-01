package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends BusinessException {
    public RefreshTokenExpiredException() {
        super("Refresh token expired", HttpStatus.BAD_REQUEST);
    }
}
