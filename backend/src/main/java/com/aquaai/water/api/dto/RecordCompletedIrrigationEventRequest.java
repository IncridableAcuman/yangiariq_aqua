package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request body for logging an already-completed historical irrigation event")
public record RecordCompletedIrrigationEventRequest(
        @NotNull(message = "Field ID is mandatory")
        @Schema(description = "Target agricultural parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Optional scheduled irrigation plan ID fulfilled by this event", example = "f1e2d3c4-b5a6-7890-1234-56789abcdef0")
        UUID irrigationPlanId,

        @NotNull(message = "Started at timestamp is mandatory")
        @Schema(description = "Execution start timestamp in ISO-8601 UTC", example = "2026-06-15T06:00:00Z")
        Instant startedAt,

        @NotNull(message = "Ended at timestamp is mandatory")
        @Schema(description = "Execution completion timestamp in ISO-8601 UTC", example = "2026-06-15T11:45:00Z")
        Instant endedAt,

        @NotNull(message = "Actual volume is mandatory for completed irrigation")
        @PositiveOrZero(message = "Actual volume must be greater than or equal to zero m³")
        @Schema(description = "Actual water volume consumed in cubic meters (m³)", example = "430.5000")
        BigDecimal actualVolumeM3,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Operational observations or meter readings", example = "Executed with siphon tubes; good soil saturation")
        String notes
) {
}
