package com.aquaai.auth.api.dto;

import com.aquaai.shared.security.UserRole;
import lombok.Builder;

import java.util.UUID;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        String fullName,
        UserRole role
) {
}
