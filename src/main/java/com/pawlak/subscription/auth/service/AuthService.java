package com.pawlak.subscription.auth.service;

import com.pawlak.subscription.auth.dto.request.LogoutRequest;
import com.pawlak.subscription.auth.dto.request.RefreshTokenRequest;
import com.pawlak.subscription.security.refresh.RefreshToken;
import com.pawlak.subscription.security.refresh.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticatedUserResponse authenticate(AuthenticateRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createInitialToken(user);

        return new AuthenticatedUserResponse(user.getRole(),jwtToken,refreshToken);
    }

    public AuthenticatedUserResponse refreshToken(RefreshTokenRequest request) {
        String rawRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.validate(rawRefreshToken);
        User user = refreshToken.getUser();

        String newRawRefreshToken = refreshTokenService.rotateToken(rawRefreshToken);
        String jwtToken = jwtService.generateToken(user);

        return new AuthenticatedUserResponse(user.getRole(),jwtToken,newRawRefreshToken);
    }

    public void logout(LogoutRequest request) {
        String rawRefreshToken = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenService.validate(rawRefreshToken);
        refreshTokenService.revoke(refreshToken);}
}