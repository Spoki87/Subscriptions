package com.pawlak.subscription.auth.controller;

import com.pawlak.subscription.auth.dto.request.AuthenticateRequest;
import com.pawlak.subscription.auth.dto.response.AuthenticatedUserResponse;
import com.pawlak.subscription.auth.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> Authenticate(@Valid @RequestBody AuthenticateRequest request) {
        AuthenticatedUserResponse response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Authenticated successfully",response, HttpStatus.OK));
    }
}
