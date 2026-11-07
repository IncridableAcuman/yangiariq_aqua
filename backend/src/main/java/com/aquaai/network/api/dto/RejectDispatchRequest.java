package com.aquaai.network.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectDispatchRequest(
        @NotBlank(message = "Rejection reason is required")
        String reason
) {}
