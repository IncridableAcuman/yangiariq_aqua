package com.aquaai.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PasswordResetConfirmRequest(
        @NotBlank(message = "Password reset token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword
) {
}
