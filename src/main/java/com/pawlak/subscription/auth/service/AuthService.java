package com.pawlak.subscription.auth.service;

import com.pawlak.subscription.security.refresh.RefreshTokenService;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.repository.UserRepository;
import com.pawlak.subscription.auth.dto.request.AuthenticateRequest;
import com.pawlak.subscription.auth.dto.response.AuthenticatedUserResponse;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
import com.pawlak.subscription.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthenticatedUserResponse authenticate(AuthenticateRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateToken(user);

        return new AuthenticatedUserResponse(user.getRole(),jwtToken, refreshToken);
    }
}