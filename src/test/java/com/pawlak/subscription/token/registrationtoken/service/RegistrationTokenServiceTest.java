package com.pawlak.subscription.token.registrationtoken.service;

import com.pawlak.subscription.email.EmailSender;
import com.pawlak.subscription.exception.domain.RegistrationTokenExpiredException;
import com.pawlak.subscription.exception.domain.RegistrationTokenNotFoundException;
import com.pawlak.subscription.token.emailbuilder.TokenEmailTemplateBuilder;
import com.pawlak.subscription.token.registrationtoken.model.RegistrationToken;
import com.pawlak.subscription.token.registrationtoken.repository.RegistrationTokenRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationTokenServiceTest {

    @Mock
    private RegistrationTokenRepository registrationTokenRepository;

    @Mock
    private TokenEmailTemplateBuilder tokenEmailTemplateBuilder;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private RegistrationTokenService registrationTokenService;

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
            when(tokenEmailTemplateBuilder.buildConfirmationEmail(any())).thenReturn("<html>confirm</html>");

            registrationTokenService.createToken(user);

            verify(tokenEmailTemplateBuilder).buildConfirmationEmail(any());
            verify(emailSender).send(eq("test@email.com"), eq("<html>confirm</html>"), eq("Confirm registration"));
            verify(registrationTokenRepository).save(any(RegistrationToken.class));
        }
    }

    @Nested
    @DisplayName("confirmRegistration")
    class ConfirmRegistration {

        @Test
        @DisplayName("enables user and deletes token when token is valid")
        void enablesUserAndDeletesToken() {
            RegistrationToken token = new RegistrationToken(user, "valid-token");

            when(registrationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

            registrationTokenService.confirmRegistration("valid-token");

            assertThat(user.isEnabled()).isTrue();
            verify(registrationTokenRepository).delete(token);
        }

        @Test
        @DisplayName("throws RegistrationTokenNotFoundException when token not found")
        void throwsWhenTokenNotFound() {
            when(registrationTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationTokenService.confirmRegistration("unknown-token"))
                    .isInstanceOf(RegistrationTokenNotFoundException.class);
        }

        @Test
        @DisplayName("throws RegistrationTokenExpiredException and deletes token when expired")
        void throwsAndDeletesWhenExpired() {
            RegistrationToken expiredToken = mock(RegistrationToken.class);
            when(expiredToken.getExpiredTime()).thenReturn(LocalDateTime.now().minusHours(1));
            when(registrationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> registrationTokenService.confirmRegistration("expired-token"))
                    .isInstanceOf(RegistrationTokenExpiredException.class);
            verify(registrationTokenRepository).delete(expiredToken);
        }
    }
}
