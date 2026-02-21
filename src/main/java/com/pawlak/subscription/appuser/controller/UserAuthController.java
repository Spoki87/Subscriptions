package com.pawlak.subscription.appuser.controller;

import com.pawlak.subscription.appuser.dto.request.AuthenticateUserRequest;
import com.pawlak.subscription.appuser.dto.response.UserResponse;
import com.pawlak.subscription.appuser.service.AuthService;
import com.pawlak.subscription.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class UserAuthController {

    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<ApiResponse<UserResponse>> authenticate(@Valid @RequestBody AuthenticateUserRequest request) {
        authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Authenticated successfully",null, HttpStatus.OK));
    }
}
