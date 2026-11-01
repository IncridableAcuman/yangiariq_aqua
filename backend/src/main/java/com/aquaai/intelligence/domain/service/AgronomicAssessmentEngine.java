package com.aquaai.intelligence.domain.service;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.AgronomicSignal;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;
import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.intelligence.domain.model.TelemetrySignal;
import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic engine synthesizing telemetry signals, trends, hydraulic safety,
 * and water budget into an explainable AgronomicAssessment.
 */
public class AgronomicAssessmentEngine {

    public record AssessmentInput(
            UUID fieldId,
            List<TelemetrySignal> latestSignals,
            TelemetryTrend soilMoistureTrend,
            boolean hasSuspectedPipeBurst,
            boolean hasCriticalPressureDrop,
            BigDecimal waterBudgetRemainingM3,
            Instant evaluationTime
    ) {}

    // Operational agronomic baselines
    private static final BigDecimal MOISTURE_CRITICAL = new BigDecimal("15.0000");
    private static final BigDecimal MOISTURE_DEFICIT = new BigDecimal("20.0000");
    private static final BigDecimal MOISTURE_SATURATED = new BigDecimal("35.0000");
    private static final BigDecimal TEMP_HEAT_WAVE = new BigDecimal("40.0000");
    private static final BigDecimal TEMP_FROST = new BigDecimal("2.0000");
    private static final BigDecimal HUMIDITY_DRY = new BigDecimal("18.0000");
    private static final BigDecimal HUMIDITY_HIGH = new BigDecimal("85.0000");
    private static final BigDecimal PRESSURE_MIN_BAR = new BigDecimal("1.2000");

    public AgronomicAssessment assess(AssessmentInput input) {
        if (input == null || input.fieldId() == null) {
            throw new IllegalArgumentException("Assessment input and fieldId must not be null");
        }

        UUID fieldId = input.fieldId();
        Instant now = input.evaluationTime() != null ? input.evaluationTime() : Instant.now();

        Map<SensorType, TelemetrySignal> signalMap = new HashMap<>();
        if (input.latestSignals() != null) {
            for (TelemetrySignal signal : input.latestSignals()) {
                if (signal != null) {
                    signalMap.put(signal.getType(), signal);
                }
            }
        }

        List<AgronomicSignal> identifiedSignals = new ArrayList<>();
        TelemetrySignal moistureSignal = signalMap.get(SensorType.SOIL_MOISTURE);
        TelemetrySignal tempSignal = signalMap.get(SensorType.TEMPERATURE);
        TelemetrySignal humiditySignal = signalMap.get(SensorType.HUMIDITY);
        TelemetrySignal pressureSignal = signalMap.get(SensorType.PRESSURE);
        TelemetrySignal flowSignal = signalMap.get(SensorType.WATER_FLOW);

        // 1. Hydraulic safety evaluations
        boolean hydraulicBlocked = false;
        String hydraulicReason = null;

        if (input.hasSuspectedPipeBurst()) {
            hydraulicBlocked = true;
            hydraulicReason = "Hydraulic Safety Interlock: SUSPECTED_PIPE_BURST anomaly active. Pressurization blocked.";
            identifiedSignals.add(AgronomicSignal.of(
                    AgronomicSignalType.ABNORMAL_FLOW,
                    IntelligenceSeverity.CRITICAL,
                    "WATER_FLOW",
                    flowSignal != null ? flowSignal.getValue() : null,
                    BigDecimal.ZERO,
                    hydraulicReason
            ));
        }

        if (input.hasCriticalPressureDrop()) {
            hydraulicBlocked = true;
            hydraulicReason = "Hydraulic Safety Interlock: Critical pressure collapse detected on distribution line.";
            identifiedSignals.add(AgronomicSignal.of(
                    AgronomicSignalType.PRESSURE_DROP,
                    IntelligenceSeverity.CRITICAL,
                    "PRESSURE",
                    pressureSignal != null ? pressureSignal.getValue() : null,
                    PRESSURE_MIN_BAR,
                    hydraulicReason
            ));
        } else if (pressureSignal != null && pressureSignal.isAuthoritative()
                && pressureSignal.getValue().compareTo(PRESSURE_MIN_BAR) < 0
                && pressureSignal.getValue().compareTo(BigDecimal.ZERO) > 0) {
            identifiedSignals.add(AgronomicSignal.of(
                    AgronomicSignalType.PRESSURE_DROP,
                    IntelligenceSeverity.HIGH,
                    "PRESSURE",
                    pressureSignal.getValue(),
                    PRESSURE_MIN_BAR,
                    "Mainline pressure is low (" + pressureSignal.getValue() + " bar)."
            ));
        }

        // 2. Soil moisture evaluation
        TelemetryFreshness moistureFreshness = moistureSignal != null ? moistureSignal.getFreshness() : TelemetryFreshness.MISSING;
        boolean moistureIsInvalid = moistureSignal != null && moistureSignal.getQuality() == ReadingQuality.INVALID;

        if (moistureSignal == null || moistureFreshness == TelemetryFreshness.MISSING) {
            identifiedSignals.add(AgronomicSignal.of(
                    AgronomicSignalType.NO_RECENT_TELEMETRY,
                    IntelligenceSeverity.HIGH,
                    "SOIL_MOISTURE",
                    null,
                    null,
                    "No recent soil moisture telemetry observed within SLA window."
            ));
        } else if (moistureIsInvalid) {
            identifiedSignals.add(AgronomicSignal.of(
                    AgronomicSignalType.NO_RECENT_TELEMETRY,
                    IntelligenceSeverity.HIGH,
                    "SOIL_MOISTURE",
                    moistureSignal.getValue(),
                    null,
                    "Soil moisture sensor reading is INVALID; disqualified from driving irrigation decisions."
            ));
        } else {
            BigDecimal moisture = moistureSignal.getValue();
            if (moisture.compareTo(MOISTURE_CRITICAL) < 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.SOIL_TOO_DRY,
                        IntelligenceSeverity.CRITICAL,
                        "SOIL_MOISTURE",
                        moisture,
                        MOISTURE_CRITICAL,
                        "Critical soil moisture deficit (" + moisture + "% < " + MOISTURE_CRITICAL + "%)."
                ));
            } else if (moisture.compareTo(MOISTURE_DEFICIT) < 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.SOIL_TOO_DRY,
                        IntelligenceSeverity.HIGH,
                        "SOIL_MOISTURE",
                        moisture,
                        MOISTURE_DEFICIT,
                        "Moderate soil moisture deficit (" + moisture + "% < " + MOISTURE_DEFICIT + "%)."
                ));
            } else if (moisture.compareTo(MOISTURE_SATURATED) > 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.SOIL_TOO_WET,
                        IntelligenceSeverity.MEDIUM,
                        "SOIL_MOISTURE",
                        moisture,
                        MOISTURE_SATURATED,
                        "Soil is saturated (" + moisture + "% > " + MOISTURE_SATURATED + "%). Withhold irrigation."
                ));
            }
        }

        // 3. Atmospheric stress evaluation
        if (tempSignal != null && !tempSignal.isDisqualified()) {
            if (tempSignal.getValue().compareTo(TEMP_HEAT_WAVE) > 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.HIGH_TEMPERATURE,
                        IntelligenceSeverity.HIGH,
                        "TEMPERATURE",
                        tempSignal.getValue(),
                        TEMP_HEAT_WAVE,
                        "Extreme ambient heat (" + tempSignal.getValue() + "°C). Garmsel heat stress active."
                ));
            } else if (tempSignal.getValue().compareTo(TEMP_FROST) < 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.LOW_TEMPERATURE,
                        IntelligenceSeverity.HIGH,
                        "TEMPERATURE",
                        tempSignal.getValue(),
                        TEMP_FROST,
                        "Frost hazard observed (" + tempSignal.getValue() + "°C). Freeze risk."
                ));
            }
        }

        if (humiditySignal != null && !humiditySignal.isDisqualified()) {
            if (humiditySignal.getValue().compareTo(HUMIDITY_DRY) < 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.LOW_HUMIDITY,
                        IntelligenceSeverity.MEDIUM,
                        "HUMIDITY",
                        humiditySignal.getValue(),
                        HUMIDITY_DRY,
                        "Extremely low relative humidity (" + humiditySignal.getValue() + "%). High atmospheric evapotranspiration."
                ));
            } else if (humiditySignal.getValue().compareTo(HUMIDITY_HIGH) > 0) {
                identifiedSignals.add(AgronomicSignal.of(
                        AgronomicSignalType.HIGH_HUMIDITY,
                        IntelligenceSeverity.LOW,
                        "HUMIDITY",
                        humiditySignal.getValue(),
                        HUMIDITY_HIGH,
                        "High relative humidity (" + humiditySignal.getValue() + "%)."
                ));
            }
        }

        // 4. Water budget constraint check
        boolean waterBudgetConstrained = false;
        String waterBudgetReason = null;
        if (input.waterBudgetRemainingM3() != null && input.waterBudgetRemainingM3().compareTo(BigDecimal.ZERO) <= 0) {
            waterBudgetConstrained = true;
            waterBudgetReason = "Volumetric water budget exhausted. Remaining available: " + input.waterBudgetRemainingM3() + " m³.";
        }

        // 5. Determine primary signal and overall severity
        AgronomicSignalType primarySignal = determinePrimarySignal(identifiedSignals, hydraulicBlocked);
        IntelligenceSeverity overallSeverity = determineOverallSeverity(identifiedSignals, hydraulicBlocked);

        // 6. Calculate transparent confidence
        BigDecimal confidenceScore = calculateConfidenceScore(
                moistureSignal,
                tempSignal,
                input.soilMoistureTrend(),
                identifiedSignals
        );
        IntelligenceConfidence confidenceLevel = deriveConfidenceLevel(confidenceScore);

        // 7. Compose summaries
        String summary = composeAgronomicSummary(primarySignal, overallSeverity, identifiedSignals);
        String recommendationsOverview = composeRecommendationsOverview(
                primarySignal,
                hydraulicBlocked,
                waterBudgetConstrained,
                confidenceLevel
        );

        return new AgronomicAssessment(
                UUID.randomUUID(),
                fieldId,
                primarySignal,
                overallSeverity,
                input.soilMoistureTrend(),
                moistureFreshness,
                confidenceLevel,
                confidenceScore,
                summary,
                recommendationsOverview,
                hydraulicBlocked,
                hydraulicReason,
                waterBudgetConstrained,
                waterBudgetReason,
                now,
                Instant.now(),
                identifiedSignals
        );
    }

    private AgronomicSignalType determinePrimarySignal(List<AgronomicSignal> signals, boolean hydraulicBlocked) {
        if (hydraulicBlocked) {
            boolean hasFlow = signals.stream().anyMatch(s -> s.getSignalType() == AgronomicSignalType.ABNORMAL_FLOW);
            return hasFlow ? AgronomicSignalType.ABNORMAL_FLOW : AgronomicSignalType.PRESSURE_DROP;
        }

        for (AgronomicSignalType candidate : List.of(
                AgronomicSignalType.SOIL_TOO_DRY,
                AgronomicSignalType.SOIL_TOO_WET,
                AgronomicSignalType.HIGH_TEMPERATURE,
                AgronomicSignalType.LOW_TEMPERATURE,
                AgronomicSignalType.NO_RECENT_TELEMETRY,
                AgronomicSignalType.LOW_HUMIDITY,
                AgronomicSignalType.HIGH_HUMIDITY
        )) {
            if (signals.stream().anyMatch(s -> s.getSignalType() == candidate)) {
                return candidate;
            }
        }

        return AgronomicSignalType.SOIL_TOO_DRY;
    }

    private IntelligenceSeverity determineOverallSeverity(List<AgronomicSignal> signals, boolean hydraulicBlocked) {
        if (hydraulicBlocked) {
            return IntelligenceSeverity.CRITICAL;
        }
        IntelligenceSeverity highest = IntelligenceSeverity.INFO;
        for (AgronomicSignal s : signals) {
            if (s.getSeverity().ordinal() > highest.ordinal()) {
                highest = s.getSeverity();
            }
        }
        return highest;
    }

    private BigDecimal calculateConfidenceScore(
            TelemetrySignal moistureSignal,
            TelemetrySignal tempSignal,
            TelemetryTrend trend,
            List<AgronomicSignal> signals
    ) {
        BigDecimal score = new BigDecimal("0.8500");

        if (moistureSignal == null || moistureSignal.getFreshness() == TelemetryFreshness.MISSING) {
            score = score.subtract(new BigDecimal("0.3500"));
        } else {
            if (moistureSignal.getQuality() == ReadingQuality.INVALID) {
                score = score.subtract(new BigDecimal("0.4000"));
            } else if (moistureSignal.getQuality() == ReadingQuality.SUSPECT) {
                score = score.subtract(new BigDecimal("0.2000"));
            }

            if (moistureSignal.getFreshness() == TelemetryFreshness.STALE) {
                score = score.subtract(new BigDecimal("0.1500"));
            }
        }

        if (trend != null && trend.getDataPointsEvaluated() >= 3) {
            score = score.add(new BigDecimal("0.0500"));
        }

        if (tempSignal != null && tempSignal.isAuthoritative()) {
            score = score.add(new BigDecimal("0.0500"));
        }

        // Clamp [0.10, 0.95]
        if (score.compareTo(new BigDecimal("0.1000")) < 0) {
            return new BigDecimal("0.1000");
        }
        if (score.compareTo(new BigDecimal("0.9500")) > 0) {
            return new BigDecimal("0.9500");
        }
        return score.setScale(4, RoundingMode.HALF_UP);
    }

    private IntelligenceConfidence deriveConfidenceLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.8000")) >= 0) {
            return IntelligenceConfidence.HIGH;
        }
        if (score.compareTo(new BigDecimal("0.5500")) >= 0) {
            return IntelligenceConfidence.MEDIUM;
        }
        return IntelligenceConfidence.LOW;
    }

    private String composeAgronomicSummary(
            AgronomicSignalType primary,
            IntelligenceSeverity severity,
            List<AgronomicSignal> signals
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Deterministic assessment: Primary signal ").append(primary)
                .append(" (Severity: ").append(severity).append("). ");
        if (!signals.isEmpty()) {
            sb.append("Identified ").append(signals.size()).append(" supporting signal(s): ");
            for (int i = 0; i < Math.min(3, signals.size()); i++) {
                if (i > 0) sb.append("; ");
                sb.append(signals.get(i).getMessage());
            }
        }
        return sb.toString();
    }

    private String composeRecommendationsOverview(
            AgronomicSignalType primary,
            boolean hydraulicBlocked,
            boolean waterBudgetConstrained,
            IntelligenceConfidence confidence
    ) {
        if (hydraulicBlocked) {
            return "CRITICAL SAFETY LOCK: Irrigation is strictly blocked by hydraulic interlock. Rectify mainline fault before dispatch.";
        }
        if (waterBudgetConstrained) {
            return "WATER ALLOCATION RESTRICTION: Seasonal parcel quota exhausted. Prioritize emergency salvage or submit allocation adjustment request.";
        }
        return switch (primary) {
            case SOIL_TOO_DRY -> "Soil moisture deficit detected. Schedule controlled irrigation run during night/cooler hours.";
            case SOIL_TOO_WET -> "Soil saturation observed. Withhold irrigation to maintain root respiration.";
            case HIGH_TEMPERATURE -> "Heat wave active. Implement light cooling hydration if soil moisture allows.";
            case LOW_TEMPERATURE -> "Frost risk active. Avoid overhead sprinkling during sub-zero intervals.";
            case NO_RECENT_TELEMETRY -> "Telemetry unavailable or invalid. Inspect hardware sensor contact before taking critical actions.";
            default -> "Parcel telemetry parameters operate within nominal agronomic baselines.";
        };
    }
}
