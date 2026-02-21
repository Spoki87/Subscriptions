package com.pawlak.subscription.appuser.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateUserRequest {
    @NotBlank(message = "username is required")
    @Size(min = 6, max = 20, message = "username must be between 6 and 20 characters long")
    String username;

    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 20, message = "password must be between 6 and 20 characters long")
    String password;
}
