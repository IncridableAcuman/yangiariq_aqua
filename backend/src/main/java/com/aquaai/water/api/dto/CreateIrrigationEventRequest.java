package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request body for starting a real-time irrigation event operation")
public record CreateIrrigationEventRequest(
        @NotNull(message = "Field ID is mandatory")
        @Schema(description = "Target agricultural parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Optional scheduled irrigation plan ID fulfilled by this event", example = "f1e2d3c4-b5a6-7890-1234-56789abcdef0")
        UUID irrigationPlanId,

        @NotNull(message = "Started at timestamp is mandatory")
        @Schema(description = "Actual operational start timestamp in ISO-8601 UTC", example = "2026-06-15T06:05:00Z")
        Instant startedAt,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Operational start notes or siphon gate opening observations", example = "Opened branch siphon gate #2")
        String notes
) {
}
