package com.pawlak.subscription.security.refresh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashServiceTest {

    private TokenHashService tokenHashService;

    @BeforeEach
    void setUp() {
        tokenHashService = new TokenHashService();
    }

    @Test
    @DisplayName("hash returns non-null, non-blank result")
    void returnsNonNullHash() {
        String result = tokenHashService.hash("some-token");

        assertThat(result).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("hash is deterministic — same input produces same output")
    void isDeterministic() {
        String first = tokenHashService.hash("test-token");
        String second = tokenHashService.hash("test-token");

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("different inputs produce different hashes")
    void differentInputsDifferentHashes() {
        String hash1 = tokenHashService.hash("token-one");
        String hash2 = tokenHashService.hash("token-two");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("hash is a valid Base64-encoded SHA-256 (44 chars with padding)")
    void hashLengthIsCorrect() {
        String result = tokenHashService.hash("any-token");

        // SHA-256 produces 32 bytes → Base64 encodes to 44 chars (with padding)
        assertThat(result).hasSize(44);
    }
}
