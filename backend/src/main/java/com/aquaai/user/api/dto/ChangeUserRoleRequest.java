package com.aquaai.user.api.dto;

import com.aquaai.shared.security.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChangeUserRoleRequest(
        @NotNull(message = "User role is required")
        UserRole role
) {
}
