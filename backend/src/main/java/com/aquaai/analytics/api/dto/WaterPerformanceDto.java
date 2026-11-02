package com.aquaai.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WaterPerformanceDto {
    private final BigDecimal plannedVolumeM3;
    private final BigDecimal actualVolumeM3;
    private final BigDecimal varianceM3;
    private final BigDecimal variancePercent;
    private final BigDecimal utilizationPercent;
    private final String utilizationStatus; // NORMAL, WARNING, OVER_CONSUMED
    private final BigDecimal totalAllocatedM3;
    private final BigDecimal totalConsumedM3;
    private final BigDecimal totalRemainingM3;
    private final BigDecimal overConsumedVolumeM3;
    private final int overConsumedFieldCount;
    private final BigDecimal averageIrrigationVolumeM3;
    private final long completedIrrigationsCount;
    private final String contextualInterpretation;

    @JsonProperty("overConsumedTotalM3")
    public BigDecimal getOverConsumedTotalM3() {
        return overConsumedVolumeM3 != null ? overConsumedVolumeM3 : BigDecimal.ZERO;
    }

    @JsonProperty("waterStatus")
    public String getWaterStatus() {
        return utilizationStatus;
    }

    @JsonProperty("statusExplanation")
    public String getStatusExplanation() {
        return contextualInterpretation;
    }

    @JsonProperty("overConsumedFieldsCount")
    public int getOverConsumedFieldsCount() {
        return overConsumedFieldCount;
    }
}
