package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Request body for completing an active irrigation delivery event")
public record CompleteIrrigationEventRequest(
        @NotNull(message = "Ended at timestamp is mandatory")
        @Schema(description = "Delivery completion timestamp in ISO-8601 UTC", example = "2026-06-15T12:15:00Z")
        Instant endedAt,

        @NotNull(message = "Actual volume is mandatory")
        @PositiveOrZero(message = "Actual volume must be greater than or equal to zero m³")
        @Schema(description = "Total physical water volume applied in cubic meters (m³)", example = "442.2500")
        BigDecimal actualVolumeM3,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Completion remarks or meter logs", example = "Watering completed with uniform furrows")
        String notes
) {
}
