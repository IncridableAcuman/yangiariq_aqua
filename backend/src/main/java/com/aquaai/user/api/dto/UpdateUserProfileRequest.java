package com.aquaai.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 64, message = "First name must not exceed 64 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 64, message = "Last name must not exceed 64 characters")
        String lastName,

        @Email(message = "Invalid email format")
        @Size(max = 128, message = "Email must not exceed 128 characters")
        String email,

        @Size(max = 64, message = "District must not exceed 64 characters")
        String district
) {
}
