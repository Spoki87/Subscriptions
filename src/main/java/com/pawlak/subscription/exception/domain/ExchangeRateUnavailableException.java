package com.pawlak.subscription.exception.domain;

import com.pawlak.subscription.exception.base.BusinessException;

public class ExchangeRateUnavailableException extends BusinessException {
    public ExchangeRateUnavailableException() {
        super("Exchange rate unavailable", null);
    }
}
