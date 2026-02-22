package com.pawlak.subscription.user.dto.response;

import com.pawlak.subscription.user.model.Role;
import lombok.Value;

import java.util.UUID;

@Value
public class UserResponse {
    UUID id;
    String email;
    Role role;
}
