package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.WaterAllocationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Detailed volumetric consumption, utilization ratio, and remaining balance for a parcel water allocation")
public record WaterAllocationConsumptionResponse(
        @Schema(description = "Allocation unique identifier", example = "e4b2d3c1-5f6a-4c7b-8910-111213141516")
        UUID allocationId,

        @Schema(description = "Associated field parcel identifier", example = "3e6077f2-a0a8-4f37-9ec8-8cb018488a51")
        UUID fieldId,

        @Schema(description = "Associated water source identifier", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID waterSourceId,

        @Schema(description = "Total assigned quota volume in cubic meters (m³)", example = "2500.0000")
        BigDecimal allocatedVolumeM3,

        @Schema(description = "Total measured delivered water volume across completed events (m³)", example = "1250.0000")
        BigDecimal consumedVolumeM3,

        @Schema(description = "Remaining available quota volume in cubic meters (m³, clamped to 0, never negative)", example = "1250.0000")
        BigDecimal remainingVolumeM3,

        @Schema(description = "Quota utilization percentage (consumed / allocated * 100)", example = "50.00")
        BigDecimal consumptionPercentage,

        @Schema(description = "True if measured water delivery strictly exceeds the assigned quota", example = "false")
        boolean isOverConsumed,

        @Schema(description = "Excess volume consumed beyond quota (max(consumed - allocated, 0))", example = "0.0000")
        BigDecimal overConsumedVolumeM3,

        @Schema(description = "Count of contributing completed irrigation events", example = "4")
        long completedEventsCount,

        @Schema(description = "Allocation lifecycle status", example = "ACTIVE")
        WaterAllocationStatus status,

        @Schema(description = "Quota validity window start timestamp in ISO-8601 UTC")
        Instant periodStart,

        @Schema(description = "Quota validity window end timestamp in ISO-8601 UTC")
        Instant periodEnd
) {
}
