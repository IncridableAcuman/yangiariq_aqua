package com.aquaai.performance;

import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.ChangeDirection;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceDataQuality;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetComplianceStatus;
import com.aquaai.performance.domain.model.TargetStatus;
import com.aquaai.performance.domain.model.TargetType;
import com.aquaai.performance.domain.service.BaselineComparabilityEngine;
import com.aquaai.performance.domain.service.ImpactAttributionEngine;
import com.aquaai.performance.domain.service.IndicatorCalculationEngine;
import com.aquaai.performance.domain.service.PerformanceEvaluationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceDomainTest {

    private IndicatorCalculationEngine calculationEngine;
    private BaselineComparabilityEngine comparabilityEngine;
    private ImpactAttributionEngine attributionEngine;
    private PerformanceEvaluationEngine evaluationEngine;

    @BeforeEach
    void setUp() {
        calculationEngine = new IndicatorCalculationEngine();
        comparabilityEngine = new BaselineComparabilityEngine();
        attributionEngine = new ImpactAttributionEngine();
        evaluationEngine = new PerformanceEvaluationEngine(calculationEngine, comparabilityEngine);
    }

    @Test
    @DisplayName("1. Delivery fulfillment calculation handles valid values and zero planned volume")
    void testDeliveryFulfillmentCalculation() {
        BigDecimal fulfillment = calculationEngine.calculateDeliveryFulfillment(
                new BigDecimal("92000.00"), new BigDecimal("100000.00")
        );
        assertThat(fulfillment).isEqualByComparingTo(new BigDecimal("92.00"));

        // Zero planned delivery
        BigDecimal zeroPlanned = calculationEngine.calculateDeliveryFulfillment(
                BigDecimal.ZERO, BigDecimal.ZERO
        );
        assertThat(zeroPlanned).isEqualByComparingTo(BigDecimal.ZERO);

        // Emergency unallocated delivery
        BigDecimal emergency = calculationEngine.calculateDeliveryFulfillment(
                new BigDecimal("5000.00"), BigDecimal.ZERO
        );
        assertThat(emergency).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("2. Conveyance loss and quota utilization calculations are exact with HALF_UP rounding")
    void testConveyanceAndQuotaCalculations() {
        BigDecimal lossPct = calculationEngine.calculateConveyanceLossPercentage(
                new BigDecimal("8000.00"), new BigDecimal("100000.00")
        );
        assertThat(lossPct).isEqualByComparingTo(new BigDecimal("8.00"));

        BigDecimal quotaUtil = calculationEngine.calculateQuotaUtilization(
                new BigDecimal("48000.00"), new BigDecimal("50000.00")
        );
        assertThat(quotaUtil).isEqualByComparingTo(new BigDecimal("96.00"));
    }

    @Test
    @DisplayName("3. Accountability metrics: verified resolution rate and recurrence rate")
    void testAccountabilityCalculations() {
        BigDecimal resRate = calculationEngine.calculateResolutionRate(9, 10);
        assertThat(resRate).isEqualByComparingTo(new BigDecimal("90.00"));

        BigDecimal recRate = calculationEngine.calculateRecurrenceRate(1, 20);
        assertThat(recRate).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("4. Change direction evaluation correctly distinguishes loss vs efficiency")
    void testChangeDirection() {
        // For losses, negative diff is favorable
        ChangeDirection lossDir = calculationEngine.evaluateChangeDirection("CCP_LOSS_PERCENTAGE", new BigDecimal("-2.50"));
        assertThat(lossDir).isEqualTo(ChangeDirection.FAVORABLE);

        // For fulfillment, positive diff is favorable
        ChangeDirection fulfillDir = calculationEngine.evaluateChangeDirection("WDP_DELIVERY_FULFILLMENT", new BigDecimal("4.00"));
        assertThat(fulfillDir).isEqualTo(ChangeDirection.FAVORABLE);
    }

    @Test
    @DisplayName("5. Baseline comparability validates duration equivalence and rejects duration mismatch > 30%")
    void testBaselineComparabilityDuration() {
        Instant now = Instant.now();
        PerformanceBaseline baseline = PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .name("Standard 30-day baseline")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeName("District")
                .periodStart(now.minus(60, ChronoUnit.DAYS))
                .periodEnd(now.minus(30, ChronoUnit.DAYS)) // 30 days duration
                .baselineValue(new BigDecimal("85.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("95.00"))
                .seasonContext("MAIN_SEASON")
                .build();

        // Target with 30 days duration -> Valid
        var validResult = comparabilityEngine.evaluateComparability(
                baseline,
                GeographicScopeType.DISTRICT,
                null,
                now.minus(30, ChronoUnit.DAYS),
                now,
                MeasurementUnit.PERCENT,
                "MAIN_SEASON"
        );
        assertThat(validResult.isComparisonValid()).isTrue();

        // Target with 10 days duration -> Mismatch > 30% -> Invalid
        var invalidResult = comparabilityEngine.evaluateComparability(
                baseline,
                GeographicScopeType.DISTRICT,
                null,
                now.minus(10, ChronoUnit.DAYS),
                now,
                MeasurementUnit.PERCENT,
                "MAIN_SEASON"
        );
        assertThat(invalidResult.isComparisonValid()).isFalse();
        assertThat(invalidResult.getReason()).contains("COMPARISON_NOT_VALID: Duration mismatch");
    }

    @Test
    @DisplayName("6. Baseline comparability rejects seasonal mismatch and unit mismatch")
    void testBaselineComparabilitySeasonalAndUnit() {
        Instant now = Instant.now();
        PerformanceBaseline baseline = PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .name("Summer baseline")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeName("District")
                .periodStart(now.minus(60, ChronoUnit.DAYS))
                .periodEnd(now.minus(30, ChronoUnit.DAYS))
                .baselineValue(new BigDecimal("85.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("95.00"))
                .seasonContext("SUMMER_VEGETATION")
                .build();

        var seasonalMismatch = comparabilityEngine.evaluateComparability(
                baseline,
                GeographicScopeType.DISTRICT,
                null,
                now.minus(30, ChronoUnit.DAYS),
                now,
                MeasurementUnit.PERCENT,
                "WINTER_CANAL_CLOSURE"
        );
        assertThat(seasonalMismatch.isComparisonValid()).isFalse();
        assertThat(seasonalMismatch.getReason()).contains("Seasonal context mismatch");

        var unitMismatch = comparabilityEngine.evaluateComparability(
                baseline,
                GeographicScopeType.DISTRICT,
                null,
                now.minus(30, ChronoUnit.DAYS),
                now,
                MeasurementUnit.M3,
                "SUMMER_VEGETATION"
        );
        assertThat(unitMismatch.isComparisonValid()).isFalse();
        assertThat(unitMismatch.getReason()).contains("Unit mismatch");
    }

    @Test
    @DisplayName("7. Impact attribution: simulated or estimated baseline CANNOT yield DIRECTLY_VERIFIED")
    void testImpactAttributionNonFabrication() {
        // Even with independent verification and favorable change, simulated baseline caps at ASSOCIATED
        AttributionLevel simLevel = attributionEngine.determineAttribution(
                BaselineQuality.SIMULATED_BASELINE,
                EvidenceClassification.OBSERVED,
                true,
                true,
                false,
                true,
                false
        );
        assertThat(simLevel).isEqualTo(AttributionLevel.ASSOCIATED);

        // Missing post-evidence yields NOT_ASSESSABLE
        AttributionLevel missingLevel = attributionEngine.determineAttribution(
                BaselineQuality.VERIFIED_BASELINE,
                EvidenceClassification.MISSING,
                true,
                true,
                false,
                true,
                false
        );
        assertThat(missingLevel).isEqualTo(AttributionLevel.NOT_ASSESSABLE);

        // Verified baseline, observed post-evidence, independent verification, no confounders -> DIRECTLY_VERIFIED
        AttributionLevel verifiedLevel = attributionEngine.determineAttribution(
                BaselineQuality.VERIFIED_BASELINE,
                EvidenceClassification.OBSERVED,
                true,
                true,
                false,
                true,
                false
        );
        assertThat(verifiedLevel).isEqualTo(AttributionLevel.DIRECTLY_VERIFIED);

        // With confounding factors -> STRONGLY_ASSOCIATED
        AttributionLevel confoundLevel = attributionEngine.determineAttribution(
                BaselineQuality.VERIFIED_BASELINE,
                EvidenceClassification.OBSERVED,
                true,
                true,
                true,
                true,
                false
        );
        assertThat(confoundLevel).isEqualTo(AttributionLevel.STRONGLY_ASSOCIATED);
    }

    @Test
    @DisplayName("8. Performance evaluation engine determines target compliance for MINIMUM, MAXIMUM, and RANGE")
    void testTargetComplianceEvaluation() {
        PerformanceTarget minTarget = PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .title("Min Delivery")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeName("District")
                .targetType(TargetType.MINIMUM)
                .targetCategory(TargetCategory.OPERATIONAL_TARGET)
                .targetValue(new BigDecimal("90.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(Instant.now())
                .targetOwner("Ops")
                .status(TargetStatus.APPROVED)
                .build();

        assertThat(evaluationEngine.evaluateTargetCompliance(minTarget, new BigDecimal("92.00")))
                .isEqualTo(TargetComplianceStatus.MET);
        assertThat(evaluationEngine.evaluateTargetCompliance(minTarget, new BigDecimal("87.00")))
                .isEqualTo(TargetComplianceStatus.WITHIN_TOLERANCE);
        assertThat(evaluationEngine.evaluateTargetCompliance(minTarget, new BigDecimal("75.00")))
                .isEqualTo(TargetComplianceStatus.UNMET);

        PerformanceTarget maxTarget = PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .title("Max Loss")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeName("Canal")
                .targetType(TargetType.MAXIMUM)
                .targetCategory(TargetCategory.ENGINEERING_CONSTRAINT)
                .targetValue(new BigDecimal("10.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(Instant.now())
                .targetOwner("Eng")
                .status(TargetStatus.APPROVED)
                .build();

        assertThat(evaluationEngine.evaluateTargetCompliance(maxTarget, new BigDecimal("8.00")))
                .isEqualTo(TargetComplianceStatus.MET);
        assertThat(evaluationEngine.evaluateTargetCompliance(maxTarget, new BigDecimal("10.40")))
                .isEqualTo(TargetComplianceStatus.WITHIN_TOLERANCE);
        assertThat(evaluationEngine.evaluateTargetCompliance(maxTarget, new BigDecimal("14.00")))
                .isEqualTo(TargetComplianceStatus.EXCEEDED);
    }

    @Test
    @DisplayName("9. Performance data quality correctly flags simulated and missing intervals")
    void testPerformanceDataQuality() {
        PerformanceDataQuality quality = PerformanceDataQuality.evaluate(
                100, 92, 4, 4, 2, new BigDecimal("85.00")
        );

        assertThat(quality.getObservedCoveragePercent()).isEqualByComparingTo(new BigDecimal("92.00"));
        assertThat(quality.isMeetsQualityThreshold()).isTrue();
        assertThat(quality.getOverallClassification()).isEqualTo(EvidenceClassification.ESTIMATED);

        PerformanceDataQuality simDominant = PerformanceDataQuality.evaluate(
                100, 20, 70, 10, 0, new BigDecimal("85.00")
        );
        assertThat(simDominant.getOverallClassification()).isEqualTo(EvidenceClassification.SIMULATED);
        assertThat(simDominant.isMeetsQualityThreshold()).isFalse();
    }
}
