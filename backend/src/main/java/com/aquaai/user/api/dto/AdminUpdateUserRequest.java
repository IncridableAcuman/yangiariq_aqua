package com.aquaai.user.api.dto;

import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AdminUpdateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 64, message = "First name must not exceed 64 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 64, message = "Last name must not exceed 64 characters")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone number must be a valid international format")
        String phoneNumber,

        @Email(message = "Invalid email format")
        @Size(max = 128, message = "Email must not exceed 128 characters")
        String email,

        @Size(max = 64, message = "District must not exceed 64 characters")
        String district,

        @Size(max = 128, message = "Organization must not exceed 128 characters")
        String organization,

        @Size(max = 64, message = "Telegram username must not exceed 64 characters")
        String telegramUsername,

        @Size(max = 8, message = "Preferred language code must not exceed 8 characters")
        String preferredLanguage,

        UserRole role,
        UserStatus status
) {
}
