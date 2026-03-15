package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BusinessException {
    public RateLimitExceededException() {
        super("Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
