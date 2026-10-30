package com.aquaai.auth.api.dto;

import com.aquaai.user.api.dto.UserProfileResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserProfileResponse user
) {
}
