package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class ResetPasswordTokenNotFoundException extends BusinessException {
    public ResetPasswordTokenNotFoundException() {
        super("Reset password token not found", HttpStatus.NOT_FOUND);
    }
}
