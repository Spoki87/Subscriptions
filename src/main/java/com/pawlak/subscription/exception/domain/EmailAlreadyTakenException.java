package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyTakenException extends BusinessException {
    public EmailAlreadyTakenException() {
        super("Email already taken", HttpStatus.CONFLICT);
    }
}
