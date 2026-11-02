package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class FieldPerformanceDto {
    private final UUID fieldId;
    private final String fieldName;
    private final String cropType;
    private final String soilType;
    private final BigDecimal areaHectares;
    private final BigDecimal allocatedM3;
    private final BigDecimal consumedM3;
    private final BigDecimal remainingM3;
    private final BigDecimal utilizationPercent;
    private final String waterStatus; // NORMAL, WARNING, OVER_CONSUMED
    private final long plansCount;
    private final long completedOperationsCount;
    private final BigDecimal planAdherencePercent;
    private final BigDecimal telemetryCoveragePercent;
    private final String sensorHealth; // ONLINE, OFFLINE, DEGRADED, NO_SENSORS
    private final long safetyEventCount;
    private final long criticalAnomalyCount;
    private final BigDecimal verificationCoveragePercent;
    private final int performanceScore; // 0..100
    private final FieldPerformanceStatus performanceStatus; // NORMAL, WARNING, ATTENTION
    private final Map<String, Integer> scoreBreakdown; // explainable components
}
