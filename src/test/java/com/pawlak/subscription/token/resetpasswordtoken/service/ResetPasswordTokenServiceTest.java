package com.pawlak.subscription.token.resetpasswordtoken.service;

import com.pawlak.subscription.email.EmailSender;
import com.pawlak.subscription.exception.domain.ResetPasswordTokenExpiredException;
import com.pawlak.subscription.exception.domain.ResetPasswordTokenNotFoundException;
import com.pawlak.subscription.token.emailbuilder.TokenEmailTemplateBuilder;
import com.pawlak.subscription.token.resetpasswordtoken.model.ResetPasswordToken;
import com.pawlak.subscription.token.resetpasswordtoken.repository.ResetPasswordTokenRepository;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordTokenServiceTest {

    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Mock
    private TokenEmailTemplateBuilder tokenEmailTemplateBuilder;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private ResetPasswordTokenService resetPasswordTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@email.com", "password", Role.USER);
    }

    @Nested
    @DisplayName("createToken")
    class CreateToken {

        @Test
        @DisplayName("builds email, sends it, and saves the token")
        void createsAndSendsToken() {
            when(tokenEmailTemplateBuilder.buildResetPasswordEmail(any())).thenReturn("<html>reset</html>");

            resetPasswordTokenService.createToken(user);

            verify(tokenEmailTemplateBuilder).buildResetPasswordEmail(any());
            verify(emailSender).send(eq("test@email.com"), eq("<html>reset</html>"), eq("Reset password"));
            verify(resetPasswordTokenRepository).save(any(ResetPasswordToken.class));
        }
    }

    @Nested
    @DisplayName("confirmToken")
    class ConfirmToken {

        @Test
        @DisplayName("deletes token when valid and not expired")
        void deletesTokenWhenValid() {
            ResetPasswordToken token = new ResetPasswordToken(user, "valid-token");
            when(resetPasswordTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

            resetPasswordTokenService.confirmToken("valid-token");

            verify(resetPasswordTokenRepository).delete(token);
        }

        @Test
        @DisplayName("throws ResetPasswordTokenNotFoundException when token not found")
        void throwsWhenTokenNotFound() {
            when(resetPasswordTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resetPasswordTokenService.confirmToken("unknown-token"))
                    .isInstanceOf(ResetPasswordTokenNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResetPasswordTokenExpiredException when token is expired")
        void throwsWhenTokenExpired() {
            ResetPasswordToken expiredToken = mock(ResetPasswordToken.class);
            when(expiredToken.getExpiredTime()).thenReturn(LocalDateTime.now().minusHours(1));
            when(resetPasswordTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> resetPasswordTokenService.confirmToken("expired-token"))
                    .isInstanceOf(ResetPasswordTokenExpiredException.class);
        }
    }
}
