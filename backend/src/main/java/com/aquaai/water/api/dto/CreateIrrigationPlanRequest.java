package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.IrrigationMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request body for scheduling an irrigation plan")
public record CreateIrrigationPlanRequest(
        @NotNull(message = "Field ID is mandatory")
        @Schema(description = "Target agricultural parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Optional parent water allocation quota ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID waterAllocationId,

        @NotNull(message = "Planned start timestamp is mandatory")
        @Schema(description = "Scheduled start timestamp in ISO-8601 UTC", example = "2026-06-15T06:00:00Z")
        Instant plannedStartAt,

        @NotNull(message = "Planned end timestamp is mandatory")
        @Schema(description = "Scheduled end timestamp in ISO-8601 UTC", example = "2026-06-15T12:00:00Z")
        Instant plannedEndAt,

        @NotNull(message = "Planned volume is mandatory")
        @Positive(message = "Planned volume must be strictly greater than zero m³")
        @Schema(description = "Target water volume in cubic meters (m³)", example = "450.0000")
        BigDecimal plannedVolumeM3,

        @NotNull(message = "Irrigation method is mandatory")
        @Schema(description = "Planned application technique", example = "FURROW")
        IrrigationMethod method,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        @Schema(description = "Planning instructions, crop vegetative stage, or notes", example = "Early morning vegetative furrow watering")
        String notes
) {
}
