package com.aquaai.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "testSecretKeyWithAtLeastFiftyTwoCharactersForShaAlgorithmSafety1234567890";

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setAccessTokenExpirationMs(3600000);
        properties.setRefreshTokenExpirationMs(7200000);
        tokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("Should generate and validate access token successfully")
    void shouldGenerateAndValidateAccessToken() {
        UUID userId = UUID.randomUUID();
        String username = "+998901234567";
        UserRole role = UserRole.FARMER;

        String token = tokenProvider.generateAccessToken(userId, username, role);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo(username);
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo(role);
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void shouldGenerateRefreshToken() {
        UUID userId = UUID.randomUUID();

        String refreshToken = tokenProvider.generateRefreshToken(userId);

        assertThat(refreshToken).isNotBlank();
        assertThat(tokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(tokenProvider.getUserIdFromToken(refreshToken)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should reject tampered or invalid token")
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.tampered.token";

        assertThat(tokenProvider.validateToken(invalidToken)).isFalse();
    }
}
