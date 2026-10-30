package com.aquaai.auth.api.dto;

import com.aquaai.shared.security.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        String email,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "Role is required")
        UserRole role,

        String district
) {
}
