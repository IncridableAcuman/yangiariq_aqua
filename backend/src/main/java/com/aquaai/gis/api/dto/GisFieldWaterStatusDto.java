package com.aquaai.gis.api.dto;

import com.aquaai.gis.domain.model.WaterStatusCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Spatial parcel water utilization and quota posturing for GIS map rendering")
public record GisFieldWaterStatusDto(
        @Schema(description = "Associated parcel field identifier")
        UUID fieldId,

        @Schema(description = "Total allocated quota volume in cubic meters (m³)", example = "10000.0000")
        BigDecimal totalAllocatedVolumeM3,

        @Schema(description = "Total delivered and consumed volume in cubic meters (m³)", example = "8500.0000")
        BigDecimal totalConsumedVolumeM3,

        @Schema(description = "Remaining available quota volume (m³, non-negative)", example = "1500.0000")
        BigDecimal totalRemainingVolumeM3,

        @Schema(description = "Excess water consumed beyond allocated quota (m³)", example = "0.0000")
        BigDecimal totalOverConsumedVolumeM3,

        @Schema(description = "Over-consumption flag", example = "false")
        boolean isOverConsumed,

        @Schema(description = "Quota utilization percentage (consumed / allocated * 100)", example = "85.00")
        BigDecimal consumptionPercentage,

        @Schema(description = "Semantic operational category for map styling", example = "WARNING")
        WaterStatusCategory category,

        @Schema(description = "Number of quota allocations in queried window", example = "1")
        long allocationCount,

        @Schema(description = "Number of completed irrigation operations in queried window", example = "3")
        long irrigationEventCount
) {
}
