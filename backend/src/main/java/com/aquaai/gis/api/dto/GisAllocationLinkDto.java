package com.aquaai.gis.api.dto;

import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Spatial linkage between an agricultural parcel and its supplying water source infrastructure")
public record GisAllocationLinkDto(
        @Schema(description = "Water allocation quota identifier")
        UUID id,

        @Schema(description = "Target parcel field identifier")
        UUID fieldId,

        @Schema(description = "Supplying water source identifier")
        UUID waterSourceId,

        @Schema(description = "Allocated water quota in cubic meters (m³)", example = "5000.0000")
        BigDecimal allocatedVolumeM3,

        @Schema(description = "Quota lifecycle status", example = "ACTIVE")
        WaterAllocationStatus status
) {
    public static GisAllocationLinkDto from(WaterAllocation allocation) {
        if (allocation == null) {
            return null;
        }
        return new GisAllocationLinkDto(
                allocation.getId(),
                allocation.getFieldId(),
                allocation.getWaterSourceId(),
                allocation.getAllocatedVolumeM3(),
                allocation.getStatus()
        );
    }
}
