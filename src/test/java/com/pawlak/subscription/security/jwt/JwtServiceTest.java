package com.pawlak.subscription.security.jwt;

import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 32-byte key (256 bits) encoded in Base64
    private static final String TEST_SECRET = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setExpirationTime(Duration.ofMinutes(15));
        jwtService = new JwtService(jwtProperties);
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET);
        user = new User("testuser", "test@email.com", "password", Role.USER);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("returns non-null token")
        void returnsNonNullToken() {
            String token = jwtService.generateToken(user);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("token contains user email as subject")
        void tokenSubjectIsEmail() {
            String token = jwtService.generateToken(user);

            assertThat(jwtService.extractUsername(token)).isEqualTo("test@email.com");
        }

        @Test
        @DisplayName("generated token is valid for the user")
        void tokenIsValidForUser() {
            String token = jwtService.generateToken(user);

            assertThat(jwtService.isTokenValid(token, user)).isTrue();
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("returns true for matching user and valid token")
        void returnsTrueForMatchingUser() {
            String token = jwtService.generateToken(user);

            assertThat(jwtService.isTokenValid(token, user)).isTrue();
        }

        @Test
        @DisplayName("returns false when email does not match")
        void returnsFalseForDifferentUser() {
            String token = jwtService.generateToken(user);
            User otherUser = new User("other", "other@email.com", "pass", Role.USER);

            assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("extracts correct email from token")
        void extractsEmail() {
            String token = jwtService.generateToken(user);

            assertThat(jwtService.extractUsername(token)).isEqualTo("test@email.com");
        }
    }
}
