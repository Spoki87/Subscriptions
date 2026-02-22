package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class RegistrationTokenExpiredException extends BusinessException {
    public RegistrationTokenExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
