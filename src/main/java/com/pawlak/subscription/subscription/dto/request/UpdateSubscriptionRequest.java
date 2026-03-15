package com.pawlak.subscription.subscription.dto.request;

import com.pawlak.subscription.currency.Currency;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class UpdateSubscriptionRequest {
    @NotBlank(message = "Subscription name is required")
    @Size(max = 100, message = "Name must not exceed {max} characters")
    String name;

    @Size(max = 500, message = "Description must not exceed {max} characters")
    String description;

    @Positive(message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    BigDecimal price;

    @NotNull(message = "Currency is required")
    Currency currency;
}
