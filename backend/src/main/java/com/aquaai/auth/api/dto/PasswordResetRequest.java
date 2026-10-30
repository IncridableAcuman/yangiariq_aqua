package com.aquaai.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PasswordResetRequest(
        @NotBlank(message = "Identifier (phone or email) is required")
        @JsonAlias({"username", "email", "phone"})
        String identifier
) {
}
