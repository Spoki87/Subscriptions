package com.pawlak.subscription.appuser.controller;

import com.pawlak.subscription.appuser.dto.request.CreateUserRequest;
import com.pawlak.subscription.appuser.dto.response.UserResponse;
import com.pawlak.subscription.appuser.service.UserService;
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
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("user registered successfully",userResponse, HttpStatus.CREATED));
    }

}
