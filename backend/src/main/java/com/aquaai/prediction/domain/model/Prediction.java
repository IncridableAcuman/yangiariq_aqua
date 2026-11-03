package com.aquaai.prediction.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable Domain Aggregate representing a predictive intelligence forecast.
 * <p>
 * Encapsulates the forecast value, range, confidence, data sufficiency, supporting evidence,
 * limitations, and non-autonomous recommended actions.
 */
@Getter
@Builder(toBuilder = true)
public class Prediction {

    private final UUID id;
    private final UUID fieldId;
    private final PredictionType predictionType;
    private final PredictionHorizon horizon;
    private final Instant targetTime;
    private final BigDecimal predictedValue;
    private final BigDecimal lowerBound;
    private final BigDecimal upperBound;
    private final String unit;
    private final int confidencePercent;
    private final RiskLevel riskLevel;
    private final DataSufficiency dataSufficiency;
    private final IrrigationLikelihood likelihood;
    private final String evidence;
    private final String assumptions;
    private final String recommendedAction;
    private final String actionTargetModule;
    private final BigDecimal actualOutcomeValue;
    private final Instant outcomeRecordedAt;
    private final PredictionAccuracyStatus accuracyStatus;
    private final Instant generatedAt;
    private final String methodVersionTag;
    private final String configurationSnapshot;

    public static String getDefaultVersionTag(PredictionType type) {
        if (type == null) return "V1";
        return switch (type) {
            case SOIL_MOISTURE_FORECAST -> "SOIL_MOISTURE_TREND_V1";
            case WATER_DEMAND_FORECAST -> "WATER_DEMAND_RULE_V1";
            case IRRIGATION_NEED_FORECAST -> "IRRIGATION_NEED_RULE_V1";
            case SENSOR_RELIABILITY_FORECAST -> "SENSOR_RELIABILITY_V1";
            case WATER_AVAILABILITY_RISK -> "WATER_AVAILABILITY_RISK_V1";
            case IRRIGATION_PLAN_RISK -> "IRRIGATION_PLAN_RISK_V1";
        };
    }

    public static String getDefaultConfigurationSnapshot(PredictionType type) {
        if (type == null) return "{}";
        return switch (type) {
            case SOIL_MOISTURE_FORECAST -> "{\"trendWindowHours\":24,\"minDepletionObservations\":3,\"smoothingAlpha\":0.3,\"kcReference\":1.00}";
            case WATER_DEMAND_FORECAST -> "{\"forecastPeriodDays\":2,\"et0ReferenceMm\":4.5,\"applicationEfficiency\":0.80,\"quotaBufferPercent\":10}";
            case IRRIGATION_NEED_FORECAST -> "{\"madThresholdFraction\":0.45,\"urgencyHours\":48,\"enforceSafetyBlock\":true}";
            case SENSOR_RELIABILITY_FORECAST -> "{\"observationWindowDays\":14,\"packetLossWarningThreshold\":0.15,\"packetLossCriticalThreshold\":0.30}";
            case WATER_AVAILABILITY_RISK -> "{\"headroomCriticalThresholdPercent\":15,\"quotaDepletionWarningPercent\":80}";
            case IRRIGATION_PLAN_RISK -> "{\"enforceSafetyInterlock\":true,\"stalenessThresholdHours\":6,\"maxDelayHours\":24}";
        };
    }

    public static Prediction create(
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            Instant targetTime,
            BigDecimal predictedValue,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            String unit,
            int confidencePercent,
            RiskLevel riskLevel,
            DataSufficiency dataSufficiency,
            IrrigationLikelihood likelihood,
            String evidence,
            String assumptions,
            String recommendedAction,
            String actionTargetModule
    ) {
        return create(
                fieldId, predictionType, horizon, targetTime,
                predictedValue, lowerBound, upperBound, unit,
                confidencePercent, riskLevel, dataSufficiency, likelihood,
                evidence, assumptions, recommendedAction, actionTargetModule,
                getDefaultVersionTag(predictionType),
                getDefaultConfigurationSnapshot(predictionType)
        );
    }

    public static Prediction create(
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            Instant targetTime,
            BigDecimal predictedValue,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            String unit,
            int confidencePercent,
            RiskLevel riskLevel,
            DataSufficiency dataSufficiency,
            IrrigationLikelihood likelihood,
            String evidence,
            String assumptions,
            String recommendedAction,
            String actionTargetModule,
            String methodVersionTag,
            String configurationSnapshot
    ) {
        return Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(Objects.requireNonNull(fieldId, "fieldId must not be null"))
                .predictionType(Objects.requireNonNull(predictionType, "predictionType must not be null"))
                .horizon(Objects.requireNonNull(horizon, "horizon must not be null"))
                .targetTime(Objects.requireNonNull(targetTime, "targetTime must not be null"))
                .predictedValue(predictedValue)
                .lowerBound(lowerBound)
                .upperBound(upperBound)
                .unit(unit != null ? unit : "")
                .confidencePercent(Math.max(0, Math.min(100, confidencePercent)))
                .riskLevel(riskLevel != null ? riskLevel : RiskLevel.UNKNOWN)
                .dataSufficiency(dataSufficiency != null ? dataSufficiency : DataSufficiency.INSUFFICIENT)
                .likelihood(likelihood != null ? likelihood : IrrigationLikelihood.UNKNOWN)
                .evidence(evidence != null ? evidence : "")
                .assumptions(assumptions != null ? assumptions : "")
                .recommendedAction(recommendedAction != null ? recommendedAction : "")
                .actionTargetModule(actionTargetModule != null ? actionTargetModule : "")
                .accuracyStatus(PredictionAccuracyStatus.PENDING)
                .generatedAt(Instant.now())
                .methodVersionTag(methodVersionTag != null ? methodVersionTag : getDefaultVersionTag(predictionType))
                .configurationSnapshot(configurationSnapshot != null ? configurationSnapshot : getDefaultConfigurationSnapshot(predictionType))
                .build();
    }

    public static Prediction createUnavailable(
            UUID fieldId,
            PredictionType predictionType,
            PredictionHorizon horizon,
            String reason
    ) {
        Instant now = Instant.now();
        Instant target = now.plus(horizon.getDuration());
        return Prediction.builder()
                .id(UUID.randomUUID())
                .fieldId(fieldId)
                .predictionType(predictionType)
                .horizon(horizon)
                .targetTime(target)
                .predictedValue(null)
                .lowerBound(null)
                .upperBound(null)
                .unit("")
                .confidencePercent(0)
                .riskLevel(RiskLevel.UNKNOWN)
                .dataSufficiency(DataSufficiency.INSUFFICIENT)
                .likelihood(IrrigationLikelihood.UNKNOWN)
                .evidence(reason != null ? reason : "Insufficient data to calculate forecast.")
                .assumptions("Missing or stale telemetry.")
                .recommendedAction("Verify sensor connectivity and telemetry stream.")
                .actionTargetModule("sensors")
                .accuracyStatus(PredictionAccuracyStatus.UNVERIFIABLE)
                .generatedAt(now)
                .methodVersionTag(getDefaultVersionTag(predictionType))
                .configurationSnapshot(getDefaultConfigurationSnapshot(predictionType))
                .build();
    }

    public Prediction recordOutcome(BigDecimal actualValue, Instant recordedAt, BigDecimal tolerance) {
        PredictionAccuracyStatus status = PredictionAccuracyStatus.EXPIRED;
        if (actualValue != null && predictedValue != null) {
            BigDecimal tol = tolerance != null ? tolerance : BigDecimal.valueOf(5.0);
            BigDecimal diff = actualValue.subtract(predictedValue).abs();
            if (diff.compareTo(tol) <= 0) {
                status = PredictionAccuracyStatus.VERIFIED_ACCURATE;
            } else {
                status = PredictionAccuracyStatus.VERIFIED_INACCURATE;
            }
        }

        return this.toBuilder()
                .actualOutcomeValue(actualValue)
                .outcomeRecordedAt(recordedAt != null ? recordedAt : Instant.now())
                .accuracyStatus(status)
                .build();
    }
}
