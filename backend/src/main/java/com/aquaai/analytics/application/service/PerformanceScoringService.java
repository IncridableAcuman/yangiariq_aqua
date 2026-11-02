package com.aquaai.analytics.application.service;

import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calculates deterministic, explainable performance indices for agricultural parcels.
 * <p>
 * Core Governance:
 * 1. Explainability: Every score is composed of visible, documented sub-factors.
 * 2. Safety Dominance: An active critical hydraulic safety issue immediately overrides the numerical score
 *    and forces the status to ATTENTION. Safety is never masked by high compliance elsewhere.
 */
@Service
public class PerformanceScoringService {

    public record ScoringResult(
            int overallScore,
            FieldPerformanceStatus status,
            Map<String, Integer> breakdown
    ) {}

    public ScoringResult calculateScore(
            BigDecimal utilizationPercent,
            BigDecimal planAdherencePercent,
            BigDecimal telemetryCoveragePercent,
            long safetyEventCount,
            long criticalAnomalyCount,
            BigDecimal verificationCoveragePercent
    ) {
        // 1. Water Quota Compliance Factor (25%)
        int waterScore;
        double util = utilizationPercent != null ? utilizationPercent.doubleValue() : 0.0;
        if (util <= 80.0) {
            waterScore = 100;
        } else if (util <= 100.0) {
            waterScore = (int) Math.round(100.0 - (util - 80.0) * 2.5); // 100 -> 50
        } else {
            waterScore = (int) Math.max(0.0, 50.0 - (util - 100.0) * 2.5);
        }

        // 2. Plan Adherence Factor (25%)
        int adherenceScore = planAdherencePercent != null ?
                (int) Math.round(Math.min(100.0, Math.max(0.0, planAdherencePercent.doubleValue()))) : 80;

        // 3. Telemetry Reliability Factor (20%)
        int telemetryScore = telemetryCoveragePercent != null ?
                (int) Math.round(Math.min(100.0, Math.max(0.0, telemetryCoveragePercent.doubleValue()))) : 50;

        // 4. Safety Record Factor (20%)
        int safetyScore;
        if (criticalAnomalyCount > 0) {
            safetyScore = 0;
        } else if (safetyEventCount > 0) {
            safetyScore = 50;
        } else {
            safetyScore = 100;
        }

        // 5. Verification Coverage Factor (10%)
        int verificationScore = verificationCoveragePercent != null ?
                (int) Math.round(Math.min(100.0, Math.max(0.0, verificationCoveragePercent.doubleValue()))) : 50;

        // Weighted Index
        int overallScore = (int) Math.round(
                (waterScore * 0.25) +
                (adherenceScore * 0.25) +
                (telemetryScore * 0.20) +
                (safetyScore * 0.20) +
                (verificationScore * 0.10)
        );
        overallScore = Math.max(0, Math.min(100, overallScore));

        // Evaluate Status with Safety Dominance
        FieldPerformanceStatus status;
        if (criticalAnomalyCount > 0 || util > 100.0) {
            status = FieldPerformanceStatus.ATTENTION;
        } else if (safetyEventCount > 0 || util > 80.0 || overallScore < 65) {
            status = FieldPerformanceStatus.WARNING;
        } else {
            status = FieldPerformanceStatus.NORMAL;
        }

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("waterCompliance", waterScore);
        breakdown.put("planAdherence", adherenceScore);
        breakdown.put("telemetryReliability", telemetryScore);
        breakdown.put("safetyRecord", safetyScore);
        breakdown.put("verificationCoverage", verificationScore);

        return new ScoringResult(overallScore, status, breakdown);
    }
}
