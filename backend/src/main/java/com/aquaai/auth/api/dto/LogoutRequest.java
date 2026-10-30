package com.aquaai.auth.api.dto;

import lombok.Builder;

@Builder
public record LogoutRequest(
        String refreshToken
) {
}
