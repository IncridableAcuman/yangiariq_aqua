package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.IrrigationPlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Comparison metrics between planned target volume and executed irrigation delivery")
public record IrrigationPlanMetricsResponse(
        @Schema(description = "Irrigation plan unique identifier", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID planId,

        @Schema(description = "Associated field parcel identifier", example = "3e6077f2-a0a8-4f37-9ec8-8cb018488a51")
        UUID fieldId,

        @Schema(description = "Associated water allocation identifier (nullable)", example = "e4b2d3c1-5f6a-4c7b-8910-111213141516")
        UUID waterAllocationId,

        @Schema(description = "Scheduled water volume in cubic meters (m³)", example = "450.0000")
        BigDecimal plannedVolumeM3,

        @Schema(description = "Total measured delivered volume across completed events (m³)", example = "420.0000")
        BigDecimal actualVolumeM3,

        @Schema(description = "Signed volumetric variance (actual - planned, negative indicates water saved)", example = "-30.0000")
        BigDecimal varianceVolumeM3,

        @Schema(description = "Absolute volumetric variance magnitude (|actual - planned|)", example = "30.0000")
        BigDecimal absoluteVarianceVolumeM3,

        @Schema(description = "Relative variance percentage ((actual - planned) / planned * 100)", example = "-6.67")
        BigDecimal variancePercentage,

        @Schema(description = "Operational consumption ratio (actual / planned * 100)", example = "93.33")
        BigDecimal consumptionRatioPercentage,

        @Schema(description = "Count of contributing completed events", example = "1")
        long completedEventsCount,

        @Schema(description = "Current irrigation plan lifecycle status", example = "COMPLETED")
        IrrigationPlanStatus status
) {
}
