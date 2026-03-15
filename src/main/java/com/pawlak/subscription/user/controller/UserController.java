package com.pawlak.subscription.user.controller;

import com.pawlak.subscription.user.dto.request.*;
import com.pawlak.subscription.user.dto.response.UserResponse;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.service.UserService;
import com.pawlak.subscription.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("user registered successfully",userResponse));
    }

    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmRegistration(@RequestParam String token) {
        userService.confirmRegistration(token);
        return ResponseEntity.ok(ApiResponse.success("Account confirmed"));
    }

    @PatchMapping("/currency")
    public ResponseEntity<ApiResponse<String>> changeCurrency(@AuthenticationPrincipal User user, @Valid @RequestBody ChangeCurrencyRequest request){
        userService.changeCurrency(request,user);
        return ResponseEntity.ok(ApiResponse.success("Currency changed"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request){
        userService.changePassword(user,request);
        return ResponseEntity.ok(ApiResponse.success("Password changed"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request){
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Code sent to your email"));
    }

    @PostMapping("/set-new-password")
    public ResponseEntity<ApiResponse<String>> setNewPassword(@AuthenticationPrincipal User user, @Valid @RequestBody NewPasswordRequest request){
        userService.setNewPassword(user,request);
        return ResponseEntity.ok(ApiResponse.success("New password set"));
    }
}
