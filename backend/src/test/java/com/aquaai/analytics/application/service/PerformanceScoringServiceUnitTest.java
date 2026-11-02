package com.aquaai.analytics.application.service;

import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceScoringServiceUnitTest {

    private PerformanceScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new PerformanceScoringService();
    }

    @Test
    @DisplayName("Perfect compliance yields high score and NORMAL status")
    void testPerfectCompliance() {
        PerformanceScoringService.ScoringResult result = scoringService.calculateScore(
                BigDecimal.valueOf(75.0), // util <= 80% -> 100
                BigDecimal.valueOf(100.0), // adherence 100% -> 100
                BigDecimal.valueOf(100.0), // telemetry 100% -> 100
                0, // no safety events -> 100
                0, // no critical anomalies
                BigDecimal.valueOf(100.0) // verification 100% -> 100
        );

        assertThat(result.overallScore()).isEqualTo(100);
        assertThat(result.status()).isEqualTo(FieldPerformanceStatus.NORMAL);
        assertThat(result.breakdown()).containsEntry("waterCompliance", 100);
        assertThat(result.breakdown()).containsEntry("planAdherence", 100);
        assertThat(result.breakdown()).containsEntry("telemetryReliability", 100);
        assertThat(result.breakdown()).containsEntry("safetyRecord", 100);
        assertThat(result.breakdown()).containsEntry("verificationCoverage", 100);
    }

    @Test
    @DisplayName("Safety Dominance: Critical anomaly immediately overrides numerical score to ATTENTION")
    void testSafetyDominance() {
        // High scores on water, adherence, telemetry, verification
        PerformanceScoringService.ScoringResult result = scoringService.calculateScore(
                BigDecimal.valueOf(70.0),
                BigDecimal.valueOf(95.0),
                BigDecimal.valueOf(95.0),
                1,
                1, // 1 critical anomaly!
                BigDecimal.valueOf(90.0)
        );

        // Safety record factor drops to 0
        assertThat(result.breakdown().get("safetyRecord")).isEqualTo(0);
        // Even if overallScore is ~75, status MUST be ATTENTION due to safety dominance
        assertThat(result.status()).isEqualTo(FieldPerformanceStatus.ATTENTION);
    }

    @Test
    @DisplayName("Water Quota Over-consumption (>100%) forces ATTENTION status")
    void testOverConsumptionDominance() {
        PerformanceScoringService.ScoringResult result = scoringService.calculateScore(
                BigDecimal.valueOf(125.0), // 125% quota utilization!
                BigDecimal.valueOf(90.0),
                BigDecimal.valueOf(90.0),
                0,
                0,
                BigDecimal.valueOf(80.0)
        );

        assertThat(result.status()).isEqualTo(FieldPerformanceStatus.ATTENTION);
        assertThat(result.breakdown().get("waterCompliance")).isLessThan(50);
    }

    @Test
    @DisplayName("Null inputs are safely handled without NullPointerException")
    void testNullInputsHandledSafely() {
        PerformanceScoringService.ScoringResult result = scoringService.calculateScore(
                null,
                null,
                null,
                0,
                0,
                null
        );

        assertThat(result.overallScore()).isBetween(0, 100);
        assertThat(result.status()).isNotNull();
        assertThat(result.breakdown()).hasSize(5);
    }
}
