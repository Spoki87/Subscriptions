package com.pawlak.subscription.subscription.dto.response;

import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class SubscriptionResponse {
    UUID id;
    String name;
    String description;
    BigDecimal price;
}
