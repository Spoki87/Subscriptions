package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class ResetPasswordTokenExpiredException extends BusinessException {
    public ResetPasswordTokenExpiredException() {
        super("Reset password token has expired", HttpStatus.BAD_REQUEST);
    }
}
