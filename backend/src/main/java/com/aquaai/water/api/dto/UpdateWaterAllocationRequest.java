package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Request body for updating active water allocation details")
public record UpdateWaterAllocationRequest(
        @NotNull(message = "Allocated volume in cubic meters is mandatory")
        @Positive(message = "Allocated volume must be strictly greater than zero m³")
        @Schema(description = "Updated volumetric quota in cubic meters (m³)", example = "2800.0000")
        BigDecimal allocatedVolumeM3,

        @NotNull(message = "Period start timestamp is mandatory")
        @Schema(description = "Updated start timestamp in ISO-8601 UTC", example = "2026-05-01T00:00:00Z")
        Instant periodStart,

        @NotNull(message = "Period end timestamp is mandatory")
        @Schema(description = "Updated end timestamp in ISO-8601 UTC", example = "2026-10-15T23:59:59Z")
        Instant periodEnd,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Updated notes or amendment reason", example = "Extended season quota approved")
        String notes
) {
}
