package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Agricultural parcel water allocation quota representation")
public record WaterAllocationResponse(
        @Schema(description = "Unique immutable identifier", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Associated field parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Supplying water source identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        UUID waterSourceId,

        @Schema(description = "Allocated water volume in cubic meters (m³)", example = "2500.0000")
        BigDecimal allocatedVolumeM3,

        @Schema(description = "Quota period start timestamp")
        Instant periodStart,

        @Schema(description = "Quota period end timestamp")
        Instant periodEnd,

        @Schema(description = "Allocation lifecycle status", example = "ACTIVE")
        WaterAllocationStatus status,

        @Schema(description = "Allocation notes or operational remarks", example = "Summer cotton allocation")
        String notes,

        @Schema(description = "Registration timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static WaterAllocationResponse from(WaterAllocation domain) {
        if (domain == null) {
            return null;
        }
        return new WaterAllocationResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getWaterSourceId(),
                domain.getAllocatedVolumeM3(),
                domain.getPeriodStart(),
                domain.getPeriodEnd(),
                domain.getStatus(),
                domain.getNotes(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
