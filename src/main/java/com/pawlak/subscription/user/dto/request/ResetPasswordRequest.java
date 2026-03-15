package com.pawlak.subscription.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class ResetPasswordRequest {
    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    String email;
}
