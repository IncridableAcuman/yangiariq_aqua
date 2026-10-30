package com.aquaai.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 64, message = "First name must not exceed 64 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 64, message = "Last name must not exceed 64 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone number must be a valid international format (e.g. +998901234567)")
        String phoneNumber,

        @Email(message = "Invalid email format")
        @Size(max = 128, message = "Email must not exceed 128 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Size(max = 64, message = "District must not exceed 64 characters")
        String district
) {
}
