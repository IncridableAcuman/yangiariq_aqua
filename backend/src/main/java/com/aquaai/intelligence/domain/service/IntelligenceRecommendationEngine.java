package com.aquaai.intelligence.domain.service;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceRecommendation;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic recommendation generator strictly conforming to the safety precedence:
 * SAFETY -> QUOTA -> INFRASTRUCTURE -> AGRONOMIC -> WEATHER -> PRIORITY -> AI.
 */
public class IntelligenceRecommendationEngine {

    public IntelligenceRecommendation generate(AgronomicAssessment assessment, BigDecimal fieldAreaHectares) {
        if (assessment == null) {
            throw new IllegalArgumentException("Assessment must not be null");
        }

        UUID fieldId = assessment.getFieldId();
        Instant now = assessment.getAssessedAt() != null ? assessment.getAssessedAt() : Instant.now();
        List<String> constraints = new ArrayList<>();
        List<AgronomicSignalType> supportingSignals = new ArrayList<>();

        if (assessment.getSignals() != null) {
            for (var s : assessment.getSignals()) {
                supportingSignals.add(s.getSignalType());
            }
        }

        BigDecimal areaHa = fieldAreaHectares != null && fieldAreaHectares.compareTo(BigDecimal.ZERO) > 0
                ? fieldAreaHectares
                : BigDecimal.ONE;

        // 1. SAFETY: Hydraulic Interlock check (authoritative, cannot be overridden)
        if (assessment.isHydraulicSafetyBlocked()) {
            constraints.add("HYDRAULIC_SAFETY_INTERLOCK_ACTIVE: " + assessment.getHydraulicSafetyReason());
            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("IRRIGATION_BLOCKED")
                    .priority(IntelligenceSeverity.CRITICAL)
                    .reason(assessment.getHydraulicSafetyReason())
                    .recommendedVolumeM3(BigDecimal.ZERO)
                    .recommendedDurationMinutes(0)
                    .recommendedWindowStart(now)
                    .recommendedWindowEnd(now.plus(8, ChronoUnit.HOURS))
                    .confidence(IntelligenceConfidence.HIGH)
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        // 2. QUOTA: Water budget constraint check
        if (assessment.isWaterBudgetConstrained()) {
            constraints.add("WATER_BUDGET_EXHAUSTED: " + assessment.getWaterBudgetReason());
            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("IRRIGATION_RESTRICTED")
                    .priority(IntelligenceSeverity.HIGH)
                    .reason(assessment.getWaterBudgetReason() + " Irrigation volume constrained to prevent quota violation.")
                    .recommendedVolumeM3(BigDecimal.ZERO)
                    .recommendedDurationMinutes(0)
                    .recommendedWindowStart(now)
                    .recommendedWindowEnd(now.plus(24, ChronoUnit.HOURS))
                    .confidence(assessment.getConfidence())
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        // 3. AGRONOMIC & WEATHER: Derived recommendations
        AgronomicSignalType primary = assessment.getPrimarySignal();

        if (primary == AgronomicSignalType.SOIL_TOO_DRY) {
            // Estimate depth of 35 mm (350 m3/ha)
            BigDecimal volumeM3 = areaHa.multiply(new BigDecimal("350.0000")).setScale(4, RoundingMode.HALF_UP);
            int durationMinutes = 240; // 4 hours baseline

            // Preferred night-time window
            Instant windowStart = now.plus(2, ChronoUnit.HOURS);
            Instant windowEnd = windowStart.plus(6, ChronoUnit.HOURS);

            constraints.add("AVOID_DAYTIME_EVAPORATION: Dispatch during evening/early morning hours");

            IntelligenceSeverity priority = assessment.getOverallSeverity() == IntelligenceSeverity.CRITICAL
                    ? IntelligenceSeverity.CRITICAL
                    : IntelligenceSeverity.HIGH;

            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("IRRIGATION_RECOMMENDED")
                    .priority(priority)
                    .reason("Soil moisture deficit verified. Controlled irrigation recommended to replenish root zone water content.")
                    .recommendedVolumeM3(volumeM3)
                    .recommendedDurationMinutes(durationMinutes)
                    .recommendedWindowStart(windowStart)
                    .recommendedWindowEnd(windowEnd)
                    .confidence(assessment.getConfidence())
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        if (primary == AgronomicSignalType.SOIL_TOO_WET) {
            constraints.add("ROOT_ASPHYXIATION_PRECAUTION: Retain current dry-down phase");
            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("DELAY_IRRIGATION")
                    .priority(IntelligenceSeverity.MEDIUM)
                    .reason("Soil saturation observed. Withhold watering until aeration levels recover.")
                    .recommendedVolumeM3(BigDecimal.ZERO)
                    .recommendedDurationMinutes(0)
                    .recommendedWindowStart(now.plus(24, ChronoUnit.HOURS))
                    .recommendedWindowEnd(now.plus(48, ChronoUnit.HOURS))
                    .confidence(assessment.getConfidence())
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        if (primary == AgronomicSignalType.HIGH_TEMPERATURE) {
            BigDecimal volumeM3 = areaHa.multiply(new BigDecimal("150.0000")).setScale(4, RoundingMode.HALF_UP);
            constraints.add("NIGHT_COOLING_ONLY: Do not irrigate between 11:00 and 17:00");
            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("PREPARE_HEAT_STRESS")
                    .priority(IntelligenceSeverity.HIGH)
                    .reason("Severe ambient heat detected. Night conditioning hydration recommended to offset transpirational shock.")
                    .recommendedVolumeM3(volumeM3)
                    .recommendedDurationMinutes(120)
                    .recommendedWindowStart(now.plus(4, ChronoUnit.HOURS))
                    .recommendedWindowEnd(now.plus(10, ChronoUnit.HOURS))
                    .confidence(assessment.getConfidence())
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        if (primary == AgronomicSignalType.NO_RECENT_TELEMETRY) {
            constraints.add("DATA_UNAVAILABLE: Verify physical sensors before committing water allocation");
            return IntelligenceRecommendation.builder()
                    .id(UUID.randomUUID())
                    .fieldId(fieldId)
                    .recommendationType("INSPECT_SENSOR")
                    .priority(IntelligenceSeverity.MEDIUM)
                    .reason("Recent telemetry missing or invalidated. Field sensor diagnostics required before scheduling irrigation.")
                    .recommendedVolumeM3(BigDecimal.ZERO)
                    .recommendedDurationMinutes(0)
                    .recommendedWindowStart(now)
                    .recommendedWindowEnd(now.plus(24, ChronoUnit.HOURS))
                    .confidence(IntelligenceConfidence.LOW)
                    .constraints(constraints)
                    .supportingSignals(supportingSignals)
                    .status("NEW")
                    .generatedAt(now)
                    .build();
        }

        // Default: Nominal baseline
        return IntelligenceRecommendation.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .recommendationType("MONITOR_NOMINAL")
                .priority(IntelligenceSeverity.INFO)
                .reason("Crop condition and soil moisture remain within optimal physiological ranges.")
                .recommendedVolumeM3(BigDecimal.ZERO)
                .recommendedDurationMinutes(0)
                .recommendedWindowStart(now)
                .recommendedWindowEnd(now.plus(24, ChronoUnit.HOURS))
                .confidence(assessment.getConfidence())
                .constraints(constraints)
                .supportingSignals(supportingSignals)
                .status("NEW")
                .generatedAt(now)
                .build();
    }
}
