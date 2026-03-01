package com.pawlak.subscription.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Valid
public class LogoutRequest {
    @NotBlank
    private String refreshToken;
}
