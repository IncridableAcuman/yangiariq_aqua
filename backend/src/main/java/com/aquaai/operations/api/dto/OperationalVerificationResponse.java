package com.aquaai.operations.api.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Post-irrigation verification response comparing planned vs. actual operational metrics
 * and before vs. after telemetry changes.
 */
@Value
@Builder
public class OperationalVerificationResponse {
    UUID planId;
    UUID fieldId;
    String fieldName;
    String cropType;
    Instant executionDate;
    
    // Volume metrics
    BigDecimal plannedVolumeM3;
    BigDecimal actualVolumeM3;
    BigDecimal volumeVarianceM3;
    BigDecimal volumeVariancePercent;

    // Duration metrics
    int plannedDurationMinutes;
    int actualDurationMinutes;
    int durationVarianceMinutes;

    // Telemetry observation
    BigDecimal soilMoistureBeforePercent;
    BigDecimal soilMoistureAfterPercent;
    BigDecimal soilMoistureChangePercent;

    // Water budget impact
    BigDecimal waterBudgetRemainingBeforeM3;
    BigDecimal waterBudgetRemainingAfterM3;

    // Verification evaluation
    @com.fasterxml.jackson.annotation.JsonProperty("isRechargeSuccessful")
    boolean isRechargeSuccessful;
    String verificationSummary;
    String learningEvidence;
}
