package com.pawlak.subscription.user.dto.request;

import com.pawlak.subscription.currency.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ChangeCurrencyRequest {
    @NotNull(message = "Currency is required")
    Currency currency;
}
