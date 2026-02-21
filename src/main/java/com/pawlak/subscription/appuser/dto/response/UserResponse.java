package com.pawlak.subscription.appuser.dto.response;

import com.pawlak.subscription.appuser.model.Role;
import lombok.Value;

import java.util.UUID;

@Value
public class UserResponse {
    UUID id;
    String email;
    Role role;
}
