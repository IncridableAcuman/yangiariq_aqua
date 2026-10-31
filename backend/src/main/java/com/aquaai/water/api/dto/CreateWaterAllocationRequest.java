package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request body for establishing a parcel volumetric water quota allocation")
public record CreateWaterAllocationRequest(
        @NotNull(message = "Field ID is mandatory")
        @Schema(description = "Agricultural parcel identifier receiving the quota", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @NotNull(message = "Water source ID is mandatory")
        @Schema(description = "Hydraulic infrastructure source supplying the water", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        UUID waterSourceId,

        @NotNull(message = "Allocated volume in cubic meters is mandatory")
        @Positive(message = "Allocated volume must be strictly greater than zero m³")
        @Schema(description = "Volumetric water quota in cubic meters (m³)", example = "2500.0000")
        BigDecimal allocatedVolumeM3,

        @NotNull(message = "Period start timestamp is mandatory")
        @Schema(description = "Quota validity window start timestamp in ISO-8601 UTC", example = "2026-05-01T00:00:00Z")
        Instant periodStart,

        @NotNull(message = "Period end timestamp is mandatory")
        @Schema(description = "Quota validity window end timestamp in ISO-8601 UTC", example = "2026-09-30T23:59:59Z")
        Instant periodEnd,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Seasonal quota notes or administrative reference", example = "Summer cotton allocation approved by district operator")
        String notes
) {
}
