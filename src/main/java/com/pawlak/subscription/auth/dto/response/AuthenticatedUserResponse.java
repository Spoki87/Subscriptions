package com.pawlak.subscription.auth.dto.response;

import com.pawlak.subscription.user.model.Role;
import lombok.Value;

@Value
public class AuthenticatedUserResponse {
    Role role;
    String token;
}
