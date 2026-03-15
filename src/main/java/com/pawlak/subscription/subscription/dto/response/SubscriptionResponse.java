package com.pawlak.subscription.subscription.dto.response;

import com.pawlak.subscription.currency.Currency;
import com.pawlak.subscription.subscription.model.SubscriptionModel;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class SubscriptionResponse {
    UUID id;
    String name;
    String description;
    BigDecimal price;
    SubscriptionModel subscriptionModel;
    Currency currency;
    BigDecimal convertedPrice;
    Currency displayCurrency;
}
