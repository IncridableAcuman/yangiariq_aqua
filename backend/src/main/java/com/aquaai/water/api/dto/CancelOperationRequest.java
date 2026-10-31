package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional request body containing administrative reason or notes for closing or cancelling an operation")
public record CancelOperationRequest(
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        @Schema(description = "Reason for state transition or cancellation notes", example = "Intake canal maintenance scheduled")
        String reason
) {
}
