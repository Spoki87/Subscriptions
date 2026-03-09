package com.pawlak.subscription.security.refresh;

import com.pawlak.subscription.exception.domain.InvalidRefreshTokenException;
import com.pawlak.subscription.exception.domain.RefreshTokenExpiredException;
import com.pawlak.subscription.security.jwt.JwtProperties;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@email.com", "password", Role.USER);
    }

    @Nested
    @DisplayName("createInitialToken")
    class CreateInitialToken {

        @Test
        @DisplayName("returns a raw token and saves hashed token to repository")
        void savesHashedTokenAndReturnsRaw() {
            when(jwtProperties.getRefreshTokenDuration()).thenReturn(Duration.ofHours(1));
            when(jwtProperties.getSessionDuration()).thenReturn(Duration.ofDays(7));
            when(tokenHashService.hash(any())).thenReturn("hashed-value");

            String rawToken = refreshTokenService.createInitialToken(user);

            assertThat(rawToken).isNotNull().isNotBlank();
            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-value");
            assertThat(captor.getValue().getUser()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("returns refresh token when valid")
        void returnsTokenWhenValid() {
            RefreshToken token = new RefreshToken(
                    "hashed",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusDays(7),
                    user
            );
            when(tokenHashService.hash("raw-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(token));

            RefreshToken result = refreshTokenService.validate("raw-token");

            assertThat(result).isEqualTo(token);
        }

        @Test
        @DisplayName("throws InvalidRefreshTokenException when token not found")
        void throwsWhenTokenNotFound() {
            when(tokenHashService.hash("unknown-token")).thenReturn("unknown-hash");
            when(refreshTokenRepository.findByTokenHash("unknown-hash")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validate("unknown-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("throws RefreshTokenExpiredException when token is expired")
        void throwsWhenTokenExpired() {
            RefreshToken expiredToken = new RefreshToken(
                    "hashed",
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusDays(7),
                    user
            );
            when(tokenHashService.hash("raw-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                    .isInstanceOf(RefreshTokenExpiredException.class);
        }

        @Test
        @DisplayName("throws RefreshTokenExpiredException when token is revoked")
        void throwsWhenTokenRevoked() {
            RefreshToken revokedToken = new RefreshToken(
                    "hashed",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusDays(7),
                    user
            );
            revokedToken.revoke();
            when(tokenHashService.hash("raw-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(revokedToken));

            assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                    .isInstanceOf(RefreshTokenExpiredException.class);
        }

        @Test
        @DisplayName("throws RefreshTokenExpiredException when session has expired")
        void throwsWhenSessionExpired() {
            RefreshToken tokenWithExpiredSession = new RefreshToken(
                    "hashed",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().minusDays(1),
                    user
            );
            when(tokenHashService.hash("raw-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(tokenWithExpiredSession));

            assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
                    .isInstanceOf(RefreshTokenExpiredException.class);
        }
    }

    @Nested
    @DisplayName("rotateToken")
    class RotateToken {

        @Test
        @DisplayName("revokes old token, saves new token, and returns new raw token")
        void rotatesSuccessfully() {
            when(jwtProperties.getRefreshTokenDuration()).thenReturn(Duration.ofHours(1));
            RefreshToken existingToken = new RefreshToken(
                    "old-hash",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusDays(7),
                    user
            );
            when(tokenHashService.hash("old-raw")).thenReturn("old-hash");
            when(refreshTokenRepository.findByTokenHash("old-hash")).thenReturn(Optional.of(existingToken));
            when(tokenHashService.hash(argThat(t -> !t.equals("old-raw")))).thenReturn("new-hash");

            String newRawToken = refreshTokenService.rotateToken("old-raw");

            assertThat(newRawToken).isNotNull().isNotEqualTo("old-raw");
            assertThat(existingToken.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("marks token as revoked and saves")
        void revokesAndSaves() {
            RefreshToken token = new RefreshToken(
                    "hashed",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusDays(7),
                    user
            );

            refreshTokenService.revoke(token);

            assertThat(token.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }
    }
}
