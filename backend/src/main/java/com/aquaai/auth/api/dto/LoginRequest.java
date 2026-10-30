package com.aquaai.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
        @NotBlank(message = "Phone number or username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
