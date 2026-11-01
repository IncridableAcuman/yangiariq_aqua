package com.aquaai.dashboard.api.dto;

import com.aquaai.irrigation.domain.model.IrrigationPriority;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class PriorityFieldDto {
    private final UUID fieldId;
    private final String fieldName;
    private final String cropType;
    private final String soilType;
    private final BigDecimal areaHectares;
    private final BigDecimal currentMoisturePercent;
    private final BigDecimal targetMoisturePercent;
    private final String moistureTrend;
    private final String telemetryFreshness;
    private final String waterStatus;
    private final BigDecimal allocatedM3;
    private final BigDecimal consumedM3;
    private final BigDecimal remainingM3;
    private final String sensorHealth;
    private final int activeAnomalyCount;
    private final String criticalAnomaly;
    private final int activeRecommendationCount;
    private final String topRecommendation;
    private final String activePlanStatus;
    private final boolean hydraulicSafetyBlocked;
    private final String hydraulicSafetyReason;
    private final IrrigationPriority priority;
    private final int priorityScore;
    private final List<String> priorityReasons;
    private final String recommendedAction;
    private final String actionTargetSection;
}
