package com.pawlak.subscription.auth.service;

import com.pawlak.subscription.auth.dto.request.AuthenticateRequest;
import com.pawlak.subscription.auth.dto.request.LogoutRequest;
import com.pawlak.subscription.auth.dto.request.RefreshTokenRequest;
import com.pawlak.subscription.auth.dto.response.AuthenticatedUserResponse;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
import com.pawlak.subscription.security.jwt.JwtService;
import com.pawlak.subscription.security.refresh.RefreshToken;
import com.pawlak.subscription.security.refresh.RefreshTokenRepository;
import com.pawlak.subscription.security.refresh.RefreshTokenService;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@email.com", "encodedPassword", Role.USER);
        refreshToken = new RefreshToken(
                "hashedToken",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(7),
                user
        );
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("authenticates user and returns tokens")
        void returnsAuthenticatedResponse() {
            AuthenticateRequest request = mock(AuthenticateRequest.class);
            when(request.getEmail()).thenReturn("test@email.com");
            when(request.getPassword()).thenReturn("password");
            when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken(user)).thenReturn("jwt-token");
            when(refreshTokenService.createInitialToken(user)).thenReturn("raw-refresh-token");

            AuthenticatedUserResponse result = authService.authenticate(request);

            assertThat(result.getAccessToken()).isEqualTo("jwt-token");
            assertThat(result.getRefreshToken()).isEqualTo("raw-refresh-token");
            assertThat(result.getRole()).isEqualTo(Role.USER);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("throws UserNotFoundException when user not found after authentication")
        void throwsWhenUserNotFound() {
            AuthenticateRequest request = mock(AuthenticateRequest.class);
            when(request.getEmail()).thenReturn("unknown@email.com");
            when(request.getPassword()).thenReturn("password");
            when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.authenticate(request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenMethod {

        @Test
        @DisplayName("validates token, rotates it, generates new JWT, and returns response")
        void returnsNewTokens() {
            RefreshTokenRequest request = mock(RefreshTokenRequest.class);
            when(request.getRefreshToken()).thenReturn("raw-refresh-token");
            when(refreshTokenService.validate("raw-refresh-token")).thenReturn(refreshToken);
            when(refreshTokenService.rotateToken("raw-refresh-token")).thenReturn("new-raw-refresh-token");
            when(jwtService.generateToken(user)).thenReturn("new-jwt-token");

            AuthenticatedUserResponse result = authService.refreshToken(request);

            assertThat(result.getAccessToken()).isEqualTo("new-jwt-token");
            assertThat(result.getRefreshToken()).isEqualTo("new-raw-refresh-token");
            assertThat(result.getRole()).isEqualTo(Role.USER);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("validates and revokes the refresh token")
        void revokesRefreshToken() {
            LogoutRequest request = mock(LogoutRequest.class);
            when(request.getRefreshToken()).thenReturn("raw-refresh-token");
            when(refreshTokenService.validate("raw-refresh-token")).thenReturn(refreshToken);

            authService.logout(request);

            verify(refreshTokenService).revoke(refreshToken);
        }
    }
}
