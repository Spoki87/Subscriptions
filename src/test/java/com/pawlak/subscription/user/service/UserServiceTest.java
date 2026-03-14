package com.pawlak.subscription.user.service;

import com.pawlak.subscription.exception.domain.EmailAlreadyTakenException;
import com.pawlak.subscription.exception.domain.InvalidPasswordException;
import com.pawlak.subscription.exception.domain.UserNotFoundException;
import com.pawlak.subscription.security.refresh.RefreshTokenService;
import com.pawlak.subscription.token.registrationtoken.service.RegistrationTokenService;
import com.pawlak.subscription.token.resetpasswordtoken.service.ResetPasswordTokenService;
import com.pawlak.subscription.user.dto.request.ChangePasswordRequest;
import com.pawlak.subscription.user.dto.request.CreateUserRequest;
import com.pawlak.subscription.user.dto.request.NewPasswordRequest;
import com.pawlak.subscription.user.dto.response.UserResponse;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import com.pawlak.subscription.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private RegistrationTokenService registrationTokenService;

    @Mock
    private ResetPasswordTokenService resetPasswordTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@email.com", "$2a$10$encodedPassword", Role.USER);
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("returns user when email exists")
        void returnsUserWhenFound() {
            when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

            UserDetails result = userService.loadUserByUsername("test@email.com");

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("throws UserNotFoundException when email not found")
        void throwsWhenNotFound() {
            when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loadUserByUsername("unknown@email.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates user, sends token, and returns UserResponse")
        void registersNewUser() {
            CreateUserRequest request = new CreateUserRequest("newuser", "new@email.com", "password");
            when(userRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());
            when(bCryptPasswordEncoder.encode("password")).thenReturn("encoded");

            UserResponse result = userService.register(request);

            assertThat(result.getEmail()).isEqualTo("new@email.com");
            assertThat(result.getRole()).isEqualTo(Role.USER);
            verify(userRepository).save(any(User.class));
            verify(registrationTokenService).createToken(any(User.class));
        }

        @Test
        @DisplayName("throws EmailAlreadyTakenException when email is taken")
        void throwsWhenEmailTaken() {
            CreateUserRequest request = new CreateUserRequest("testuser", "test@email.com", "password");
            when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(EmailAlreadyTakenException.class);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("changes password when current password matches")
        void changesPasswordSuccessfully() {
            ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword");
            when(bCryptPasswordEncoder.matches("currentPassword", user.getPassword())).thenReturn(true);
            when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNew");

            userService.changePassword(user, request);

            assertThat(user.getPassword()).isEqualTo("encodedNew");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws InvalidPasswordException when current password does not match")
        void throwsWhenWrongCurrentPassword() {
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword");
            when(bCryptPasswordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(user, request))
                    .isInstanceOf(InvalidPasswordException.class);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("confirmRegistration")
    class ConfirmRegistration {

        @Test
        @DisplayName("delegates to registrationTokenService")
        void delegatesToTokenService() {
            userService.confirmRegistration("some-token");

            verify(registrationTokenService).confirmRegistration("some-token");
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("delegates to resetPasswordTokenService")
        void delegatesToTokenService() {
            userService.resetPassword(user);

            verify(resetPasswordTokenService).createToken(user);
        }
    }

    @Nested
    @DisplayName("setNewPassword")
    class SetNewPassword {

        @Test
        @DisplayName("confirms token, updates password, and saves user")
        void setsNewPasswordSuccessfully() {
            NewPasswordRequest request = new NewPasswordRequest("newPassword", "reset-token");
            when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNew");

            userService.setNewPassword(user, request);

            verify(resetPasswordTokenService).confirmToken("reset-token");
            assertThat(user.getPassword()).isEqualTo("encodedNew");
            verify(userRepository).save(user);
        }
    }
}
