package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException() {
        super("Invalid password", HttpStatus.BAD_REQUEST);
    }
}
