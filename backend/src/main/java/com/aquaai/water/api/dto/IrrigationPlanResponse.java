package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.IrrigationMethod;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Scheduled parcel irrigation plan representation")
public record IrrigationPlanResponse(
        @Schema(description = "Unique immutable identifier", example = "f1e2d3c4-b5a6-7890-1234-56789abcdef0")
        UUID id,

        @Schema(description = "Associated field parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Optional parent water allocation quota ID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID waterAllocationId,

        @Schema(description = "Scheduled start timestamp in ISO-8601 UTC")
        Instant plannedStartAt,

        @Schema(description = "Scheduled end timestamp in ISO-8601 UTC")
        Instant plannedEndAt,

        @Schema(description = "Planned volume in cubic meters (m³)", example = "450.0000")
        BigDecimal plannedVolumeM3,

        @Schema(description = "Planned application technique", example = "FURROW")
        IrrigationMethod method,

        @Schema(description = "Plan lifecycle status", example = "PLANNED")
        IrrigationPlanStatus status,

        @Schema(description = "Planning remarks or stage notes", example = "Early morning furrow watering")
        String notes,

        @Schema(description = "Plan creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static IrrigationPlanResponse from(IrrigationPlan domain) {
        if (domain == null) {
            return null;
        }
        return new IrrigationPlanResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getWaterAllocationId(),
                domain.getPlannedStartAt(),
                domain.getPlannedEndAt(),
                domain.getPlannedVolumeM3(),
                domain.getMethod(),
                domain.getStatus(),
                domain.getNotes(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
