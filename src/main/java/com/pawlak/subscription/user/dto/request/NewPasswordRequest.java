package com.pawlak.subscription.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class NewPasswordRequest {
    @NotBlank(message = "Password is required to set new password")
    String newPassword;

    @NotBlank(message = "Token required")
    String token;
}
