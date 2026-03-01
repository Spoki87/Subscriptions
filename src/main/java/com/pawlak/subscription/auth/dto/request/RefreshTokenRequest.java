package com.pawlak.subscription.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Valid
@Getter
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    String refreshToken;
}
