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
