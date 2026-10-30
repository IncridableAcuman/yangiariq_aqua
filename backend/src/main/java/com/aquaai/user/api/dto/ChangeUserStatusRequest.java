package com.aquaai.user.api.dto;

import com.aquaai.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChangeUserStatusRequest(
        @NotNull(message = "User status is required")
        UserStatus status,

        String reason
) {
}
