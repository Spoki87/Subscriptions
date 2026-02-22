package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class RegistrationTokenNotFoundException extends BusinessException {
    public RegistrationTokenNotFoundException() {
        super("Registration token not found", HttpStatus.NOT_FOUND);
    }
}
