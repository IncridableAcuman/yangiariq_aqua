package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.IrrigationEventOutcome;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SmartPlanOutcome;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Domain Service responsible for matching an individual prediction with its actual authoritative outcome.
 * <p>
 * Enforces strict per-prediction-type outcome matching semantics:
 * - Soil Moisture: nearest valid sensor reading within ±3h tolerance window.
 * - Irrigation Need: qualifying operation within horizon, preserving hydraulic safety blocks.
 * - Water Demand: aggregated consumed volume vs predicted demand and human approval differences.
 * - Sensor Reliability: observed 7-14d telemetry quality and offline events.
 */
public class PredictionOutcomeMatcher {

    private static final Duration SOIL_MOISTURE_TOLERANCE_WINDOW = Duration.ofHours(3);
    private static final Duration IRRIGATION_GRACE_PERIOD = Duration.ofHours(6);

    public String resolveModelVersion(Prediction prediction) {
        if (prediction.getMethodVersionTag() != null && !prediction.getMethodVersionTag().isBlank()) {
            return prediction.getMethodVersionTag();
        }
        return Prediction.getDefaultVersionTag(prediction.getPredictionType());
    }

    public PredictionEvaluation matchAndEvaluate(Prediction prediction, PredictionDataProvider dataProvider) {
        Objects.requireNonNull(prediction, "prediction must not be null");
        Objects.requireNonNull(dataProvider, "dataProvider must not be null");

        Instant now = Instant.now();
        Instant targetTime = prediction.getTargetTime();

        // 1. Check if target horizon has transpired
        if (targetTime.isAfter(now)) {
            return PredictionEvaluation.builder()
                    .id(java.util.UUID.randomUUID())
                    .predictionId(prediction.getId())
                    .fieldId(prediction.getFieldId())
                    .predictionType(prediction.getPredictionType())
                    .horizon(prediction.getHorizon())
                    .modelVersion(resolveModelVersion(prediction))
                    .generatedAt(prediction.getGeneratedAt())
                    .targetTime(targetTime)
                    .evaluatedAt(now)
                    .confidencePercent(prediction.getConfidencePercent())
                    .evaluationStatus(EvaluationStatus.PENDING)
                    .failureReason(EvaluationFailureReason.NONE)
                    .dataQuality(EvaluationDataQuality.DATA_COMPLETE)
                    .evaluationMethod("PENDING_HORIZON_CLOSE")
                    .notes("Prediction target horizon has not yet transpired.")
                    .build();
        }

        return switch (prediction.getPredictionType()) {
            case SOIL_MOISTURE_FORECAST -> evaluateSoilMoisture(prediction, dataProvider);
            case IRRIGATION_NEED_FORECAST -> evaluateIrrigationNeed(prediction, dataProvider);
            case WATER_DEMAND_FORECAST -> evaluateWaterDemand(prediction, dataProvider);
            case SENSOR_RELIABILITY_FORECAST -> evaluateSensorReliability(prediction, dataProvider);
            case WATER_AVAILABILITY_RISK -> evaluateWaterAvailability(prediction, dataProvider);
            case IRRIGATION_PLAN_RISK -> evaluateIrrigationPlanRisk(prediction, dataProvider);
        };
    }

    private PredictionEvaluation evaluateSoilMoisture(Prediction prediction, PredictionDataProvider dataProvider) {
        Instant targetTime = prediction.getTargetTime();
        Instant windowStart = targetTime.minus(SOIL_MOISTURE_TOLERANCE_WINDOW);
        Instant windowEnd = targetTime.plus(SOIL_MOISTURE_TOLERANCE_WINDOW);

        List<TelemetryObservation> readings = dataProvider.getMoistureObservationsInWindow(
                prediction.getFieldId(), windowStart, windowEnd);

        if (readings.isEmpty()) {
            return PredictionEvaluation.createInsufficientData(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    targetTime,
                    prediction.getConfidencePercent(),
                    EvaluationFailureReason.MISSING_SENSOR,
                    "No soil moisture telemetry available in tolerance window [target ±3h]."
            );
        }

        // Find nearest valid reading to target time
        TelemetryObservation bestReading = readings.stream()
                .filter(r -> "VALID".equalsIgnoreCase(r.quality()))
                .min(Comparator.comparingLong(r -> Math.abs(Duration.between(r.measuredAt(), targetTime).toMillis())))
                .orElse(null);

        EvaluationDataQuality quality = EvaluationDataQuality.DATA_COMPLETE;
        if (bestReading == null) {
            // Fallback to suspect reading if available
            bestReading = readings.stream()
                    .min(Comparator.comparingLong(r -> Math.abs(Duration.between(r.measuredAt(), targetTime).toMillis())))
                    .orElse(null);
            quality = EvaluationDataQuality.DATA_PARTIAL;
        } else {
            long deltaMinutes = Math.abs(Duration.between(bestReading.measuredAt(), targetTime).toMinutes());
            if (deltaMinutes > 60) {
                quality = EvaluationDataQuality.DATA_PARTIAL;
            }
        }

        if (bestReading == null || bestReading.value() == null) {
            return PredictionEvaluation.createInsufficientData(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    targetTime,
                    prediction.getConfidencePercent(),
                    EvaluationFailureReason.STALE_TELEMETRY,
                    "Only invalid or null sensor readings available in tolerance window."
            );
        }

        return PredictionEvaluation.createNumerical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                targetTime,
                bestReading.measuredAt(),
                prediction.getPredictedValue(),
                prediction.getLowerBound(),
                prediction.getUpperBound(),
                bestReading.value(),
                null,
                null,
                null,
                "Matched nearest soil moisture observation recorded at " + bestReading.measuredAt(),
                false,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                quality,
                "NEAREST_OBSERVATION_WITHIN_TOLERANCE",
                "Evaluated against sensor reading: " + bestReading.value() + "%"
        );
    }

    private PredictionEvaluation evaluateIrrigationNeed(Prediction prediction, PredictionDataProvider dataProvider) {
        Instant windowStart = prediction.getGeneratedAt();
        Instant windowEnd = prediction.getTargetTime().plus(IRRIGATION_GRACE_PERIOD);

        List<IrrigationEventOutcome> events = dataProvider.getIrrigationEventsInWindow(
                prediction.getFieldId(), windowStart, windowEnd);

        List<HydraulicAnomalyInfo> anomalies = dataProvider.getHydraulicAnomaliesInWindow(
                prediction.getFieldId(), windowStart, windowEnd);

        List<SmartPlanOutcome> plans = dataProvider.getSmartPlansInWindow(
                prediction.getFieldId(), windowStart, windowEnd);

        boolean qualifyingEventOccurred = events.stream()
                .anyMatch(e -> "COMPLETED".equalsIgnoreCase(e.status()) || "IN_PROGRESS".equalsIgnoreCase(e.status()));

        boolean safetyInterlockTriggered = anomalies.stream()
                .anyMatch(a -> "CRITICAL".equalsIgnoreCase(a.severity()) || "HIGH".equalsIgnoreCase(a.severity()))
                || plans.stream().anyMatch(SmartPlanOutcome::isBlocked);

        boolean predictedPositive = prediction.getLikelihood() == IrrigationLikelihood.LIKELY
                || prediction.getLikelihood() == IrrigationLikelihood.HIGHLY_LIKELY;

        String predictedClass = prediction.getLikelihood() != null ? prediction.getLikelihood().name() : "UNKNOWN";
        String actualClass = qualifyingEventOccurred ? "IRRIGATED" : "NO_IRRIGATION";

        if (predictedPositive && qualifyingEventOccurred) {
            IrrigationEventOutcome firstEvent = events.get(0);
            return PredictionEvaluation.createCategorical(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    prediction.getTargetTime(),
                    firstEvent.startedAt(),
                    predictedClass,
                    actualClass,
                    PredictionClassificationResult.TRUE_POSITIVE,
                    prediction.getPredictedValue(),
                    firstEvent.actualVolumeM3(),
                    firstEvent.eventId() != null ? firstEvent.eventId().toString() : null,
                    "Qualifying irrigation event occurred within horizon window.",
                    false,
                    prediction.getConfidencePercent(),
                    EvaluationStatus.EVALUATED,
                    EvaluationFailureReason.NONE,
                    EvaluationDataQuality.DATA_COMPLETE,
                    "CONFUSION_MATRIX_MATCHING",
                    "Predicted irrigation correctly confirmed by operational event."
            );
        }

        if (!predictedPositive && !qualifyingEventOccurred) {
            return PredictionEvaluation.createCategorical(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    prediction.getTargetTime(),
                    prediction.getTargetTime(),
                    predictedClass,
                    actualClass,
                    PredictionClassificationResult.TRUE_NEGATIVE,
                    null,
                    null,
                    null,
                    "No irrigation occurred, in accordance with prediction.",
                    false,
                    prediction.getConfidencePercent(),
                    EvaluationStatus.EVALUATED,
                    EvaluationFailureReason.NONE,
                    EvaluationDataQuality.DATA_COMPLETE,
                    "CONFUSION_MATRIX_MATCHING",
                    "Correctly forecasted no irrigation needed."
            );
        }

        if (predictedPositive && !qualifyingEventOccurred) {
            // OPERATIONAL GOVERNANCE (Section 19 & 53):
            // Check if hydraulic safety interlock or block prevented the operation
            if (safetyInterlockTriggered) {
                String blockedReason = plans.stream()
                        .filter(SmartPlanOutcome::isBlocked)
                        .map(SmartPlanOutcome::blockedReason)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("Hydraulic safety interlock or anomaly blocked execution.");

                return PredictionEvaluation.createCategorical(
                        prediction.getId(),
                        prediction.getFieldId(),
                        prediction.getPredictionType(),
                        prediction.getHorizon(),
                        resolveModelVersion(prediction),
                        prediction.getGeneratedAt(),
                        prediction.getTargetTime(),
                        prediction.getTargetTime(),
                        predictedClass,
                        "INTERRUPTED_BY_SAFETY",
                        PredictionClassificationResult.INTERRUPTED,
                        prediction.getPredictedValue(),
                        null,
                        null,
                        "Predicted irrigation was not executed due to safety interlock: " + blockedReason,
                        true,
                        prediction.getConfidencePercent(),
                        EvaluationStatus.OUTCOME_INTERRUPTED,
                        EvaluationFailureReason.SAFETY_EVENT,
                        EvaluationDataQuality.DATA_COMPLETE,
                        "OPERATIONAL_INTERRUPTION_AUDIT",
                        "Operation interrupted by hydraulic safety rule; not a model false positive."
                );
            }

            // Real False Positive
            return PredictionEvaluation.createCategorical(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    prediction.getTargetTime(),
                    prediction.getTargetTime(),
                    predictedClass,
                    actualClass,
                    PredictionClassificationResult.FALSE_POSITIVE,
                    prediction.getPredictedValue(),
                    BigDecimal.ZERO,
                    null,
                    "Irrigation predicted but no operational event occurred without safety impediment.",
                    false,
                    prediction.getConfidencePercent(),
                    EvaluationStatus.EVALUATED,
                    EvaluationFailureReason.NONE,
                    EvaluationDataQuality.DATA_COMPLETE,
                    "CONFUSION_MATRIX_MATCHING",
                    "False Positive: moisture depletion may have progressed slower than expected."
            );
        }

        // False Negative: Predicted not expected, but irrigation occurred
        IrrigationEventOutcome unpredictedEvent = events.get(0);
        return PredictionEvaluation.createCategorical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                prediction.getTargetTime(),
                unpredictedEvent.startedAt(),
                predictedClass,
                actualClass,
                PredictionClassificationResult.FALSE_NEGATIVE,
                null,
                unpredictedEvent.actualVolumeM3(),
                unpredictedEvent.eventId() != null ? unpredictedEvent.eventId().toString() : null,
                "Irrigation event occurred despite forecast indicating low or no likelihood.",
                false,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "CONFUSION_MATRIX_MATCHING",
                "False Negative: unforeseen water demand or farmer manual intervention."
        );
    }

    private PredictionEvaluation evaluateWaterDemand(Prediction prediction, PredictionDataProvider dataProvider) {
        Instant from = prediction.getGeneratedAt();
        Instant to = prediction.getTargetTime();

        BigDecimal actualConsumption = dataProvider.getActualWaterConsumptionInWindow(prediction.getFieldId(), from, to);
        List<SmartPlanOutcome> plans = dataProvider.getSmartPlansInWindow(prediction.getFieldId(), from, to);

        BigDecimal recommendedVol = null;
        BigDecimal approvedVol = null;
        String planIdStr = null;
        String operationalNotes = "Aggregated water consumption across forecast horizon window.";

        if (!plans.isEmpty()) {
            SmartPlanOutcome plan = plans.get(0);
            recommendedVol = plan.recommendedVolumeM3();
            approvedVol = plan.recommendedVolumeM3(); // In existing smart plans, approved adopts recommended unless overridden
            planIdStr = plan.planId() != null ? plan.planId().toString() : null;
            operationalNotes = "Matched with smart irrigation plan " + planIdStr + " in horizon window.";
        }

        return PredictionEvaluation.createNumerical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                prediction.getTargetTime(),
                prediction.getTargetTime(),
                prediction.getPredictedValue(),
                prediction.getLowerBound(),
                prediction.getUpperBound(),
                actualConsumption != null ? actualConsumption : BigDecimal.ZERO,
                recommendedVol,
                approvedVol,
                planIdStr,
                operationalNotes,
                false,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "WATER_CONSUMPTION_AGGREGATION",
                "Demand forecast vs actual delivery: predicted=" + prediction.getPredictedValue() + "m³, actual=" + actualConsumption + "m³"
        );
    }

    private PredictionEvaluation evaluateSensorReliability(Prediction prediction, PredictionDataProvider dataProvider) {
        List<SensorHealthSummary> summaries = dataProvider.getSensorHealthHistory(
                prediction.getFieldId(), prediction.getGeneratedAt());

        if (summaries.isEmpty()) {
            return PredictionEvaluation.createInsufficientData(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    prediction.getTargetTime(),
                    prediction.getConfidencePercent(),
                    EvaluationFailureReason.MISSING_SENSOR,
                    "No sensors registered on field for reliability verification."
            );
        }

        boolean predictedElevatedRisk = prediction.getRiskLevel() == RiskLevel.HIGH
                || prediction.getRiskLevel() == RiskLevel.CRITICAL;

        // An observed failure means either offline intervals or > 20% invalid readings
        boolean actualFailureObserved = summaries.stream().anyMatch(s ->
                s.offlinePeriodsCount() > 0 || (s.totalReadings14d() > 0 && ((double) s.invalidReadings14d() / s.totalReadings14d()) > 0.20));

        PredictionClassificationResult result;
        if (predictedElevatedRisk && actualFailureObserved) {
            result = PredictionClassificationResult.TRUE_POSITIVE;
        } else if (!predictedElevatedRisk && !actualFailureObserved) {
            result = PredictionClassificationResult.TRUE_NEGATIVE;
        } else if (predictedElevatedRisk && !actualFailureObserved) {
            result = PredictionClassificationResult.FALSE_POSITIVE;
        } else {
            result = PredictionClassificationResult.FALSE_NEGATIVE;
        }

        return PredictionEvaluation.createCategorical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                prediction.getTargetTime(),
                prediction.getTargetTime(),
                prediction.getRiskLevel() != null ? prediction.getRiskLevel().name() : "UNKNOWN",
                actualFailureObserved ? "UNRELIABLE" : "RELIABLE",
                result,
                null,
                null,
                null,
                "Observed sensor telemetry quality and offline events during horizon.",
                false,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "TELEMETRY_QUALITY_AUDIT",
                "Evaluated sensor drift against telemetry error rate."
        );
    }

    private PredictionEvaluation evaluateWaterAvailability(Prediction prediction, PredictionDataProvider dataProvider) {
        var waterBalanceOpt = dataProvider.getWaterBalance(prediction.getFieldId());
        if (waterBalanceOpt.isEmpty()) {
            return PredictionEvaluation.createInsufficientData(
                    prediction.getId(),
                    prediction.getFieldId(),
                    prediction.getPredictionType(),
                    prediction.getHorizon(),
                    resolveModelVersion(prediction),
                    prediction.getGeneratedAt(),
                    prediction.getTargetTime(),
                    prediction.getConfidencePercent(),
                    EvaluationFailureReason.MISSING_OUTCOME,
                    "Water allocation data not found for parcel."
            );
        }

        boolean actualOverConsumed = waterBalanceOpt.get().isOverConsumed();
        boolean predictedRisk = prediction.getRiskLevel() == RiskLevel.HIGH || prediction.getRiskLevel() == RiskLevel.CRITICAL;

        PredictionClassificationResult result;
        if (predictedRisk && actualOverConsumed) {
            result = PredictionClassificationResult.TRUE_POSITIVE;
        } else if (!predictedRisk && !actualOverConsumed) {
            result = PredictionClassificationResult.TRUE_NEGATIVE;
        } else if (predictedRisk && !actualOverConsumed) {
            result = PredictionClassificationResult.FALSE_POSITIVE;
        } else {
            result = PredictionClassificationResult.FALSE_NEGATIVE;
        }

        return PredictionEvaluation.createCategorical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                prediction.getTargetTime(),
                prediction.getTargetTime(),
                prediction.getRiskLevel().name(),
                actualOverConsumed ? "EXHAUSTED" : "WITHIN_QUOTA",
                result,
                null,
                null,
                null,
                "Seasonal quota headroom status at horizon end.",
                false,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "QUOTA_DEFICIT_CHECK",
                "Evaluated against water allocation balance."
        );
    }

    private PredictionEvaluation evaluateIrrigationPlanRisk(Prediction prediction, PredictionDataProvider dataProvider) {
        List<SmartPlanOutcome> plans = dataProvider.getSmartPlansInWindow(
                prediction.getFieldId(), prediction.getGeneratedAt(), prediction.getTargetTime());

        boolean hasBlockedOrCancelled = plans.stream().anyMatch(p ->
                p.isBlocked() || "CANCELLED".equalsIgnoreCase(p.status()) || "FAILED".equalsIgnoreCase(p.status()));

        boolean predictedRisk = prediction.getRiskLevel() == RiskLevel.HIGH || prediction.getRiskLevel() == RiskLevel.CRITICAL;

        PredictionClassificationResult result;
        if (predictedRisk && hasBlockedOrCancelled) {
            result = PredictionClassificationResult.TRUE_POSITIVE;
        } else if (!predictedRisk && !hasBlockedOrCancelled) {
            result = PredictionClassificationResult.TRUE_NEGATIVE;
        } else if (predictedRisk && !hasBlockedOrCancelled) {
            result = PredictionClassificationResult.FALSE_POSITIVE;
        } else {
            result = PredictionClassificationResult.FALSE_NEGATIVE;
        }

        return PredictionEvaluation.createCategorical(
                prediction.getId(),
                prediction.getFieldId(),
                prediction.getPredictionType(),
                prediction.getHorizon(),
                resolveModelVersion(prediction),
                prediction.getGeneratedAt(),
                prediction.getTargetTime(),
                prediction.getTargetTime(),
                prediction.getRiskLevel().name(),
                hasBlockedOrCancelled ? "EXECUTION_FAILED" : "EXECUTION_NORMAL",
                result,
                null,
                null,
                null,
                "Plan execution status and interlocks evaluated.",
                hasBlockedOrCancelled,
                prediction.getConfidencePercent(),
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "PLAN_INTERLOCK_AUDIT",
                "Evaluated against smart irrigation plan execution logs."
        );
    }
}
