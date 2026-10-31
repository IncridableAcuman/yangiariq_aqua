package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Aggregated periodic water consumption, remaining balance, and efficiency metrics for an agricultural parcel")
public record FieldWaterSummaryResponse(
        @Schema(description = "Associated field parcel identifier", example = "3e6077f2-a0a8-4f37-9ec8-8cb018488a51")
        UUID fieldId,

        @Schema(description = "Query window period (null bounds if unbounded)")
        DateRangeDto period,

        @Schema(description = "Total allocated water quota in cubic meters (m³)", example = "5000.0000")
        BigDecimal totalAllocatedVolumeM3,

        @Schema(description = "Total actual water volume delivered across completed operations (m³)", example = "3450.0000")
        BigDecimal totalConsumedVolumeM3,

        @Schema(description = "Total remaining available quota in cubic meters (m³, never negative)", example = "1550.0000")
        BigDecimal totalRemainingVolumeM3,

        @Schema(description = "Total excess water volume consumed beyond allocated quotas (m³)", example = "0.0000")
        BigDecimal totalOverConsumedVolumeM3,

        @Schema(description = "Flag indicating whether field consumption has exceeded total allocated quotas", example = "false")
        boolean isOverConsumed,

        @Schema(description = "Aggregated quota consumption percentage (consumed / allocated * 100)", example = "69.00")
        BigDecimal consumptionPercentage,

        @Schema(description = "Number of active/closed quota allocations in the period", example = "2")
        long allocationCount,

        @Schema(description = "Number of completed irrigation operations in the period", example = "8")
        long irrigationEventCount
) {
}
