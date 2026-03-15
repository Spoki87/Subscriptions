package com.pawlak.subscription.auth.dto.response;

import com.pawlak.subscription.currency.Currency;
import com.pawlak.subscription.user.model.Role;
import lombok.Value;

@Value
public class AuthenticatedUserResponse {
    Role role;
    Currency currency;
    String accessToken;
    String refreshToken;
}
