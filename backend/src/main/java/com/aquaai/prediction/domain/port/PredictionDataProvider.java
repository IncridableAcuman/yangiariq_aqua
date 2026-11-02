package com.aquaai.prediction.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound domain port abstracting access to operational data required by the predictive engine
 * and continuous intelligence evaluation.
 */
public interface PredictionDataProvider {

    record FieldMetadata(
            UUID fieldId,
            UUID ownerId,
            String name,
            String cropType,
            String soilType,
            String irrigationMethod,
            BigDecimal areaHectares,
            boolean isActive
    ) {}

    record TelemetryObservation(
            UUID sensorId,
            String sensorType,
            BigDecimal value,
            String quality, // VALID, SUSPECT, INVALID
            Instant measuredAt
    ) {}

    record WaterBalance(
            UUID allocationId,
            BigDecimal allocatedVolumeM3,
            BigDecimal consumedVolumeM3,
            BigDecimal remainingVolumeM3,
            BigDecimal committedVolumeM3,
            boolean isOverConsumed
    ) {}

    record UpcomingPlanInfo(
            UUID planId,
            UUID fieldId,
            Instant plannedStartAt,
            BigDecimal recommendedVolumeM3,
            String status,
            boolean isBlocked,
            String blockedReason
    ) {}

    record HydraulicAnomalyInfo(
            UUID anomalyId,
            UUID fieldId,
            String anomalyType,
            String severity,
            Instant detectedAt,
            boolean isResolved
    ) {}

    record SensorHealthSummary(
            UUID sensorId,
            UUID fieldId,
            String sensorName,
            String status,
            Instant lastSeenAt,
            long totalReadings14d,
            long validReadings14d,
            long suspectReadings14d,
            long invalidReadings14d,
            int stalePeriodsCount,
            int offlinePeriodsCount
    ) {}

    record WeatherContext(
            BigDecimal temperatureC,
            BigDecimal relativeHumidityPercent,
            BigDecimal referenceEt0MmDay,
            BigDecimal forecastRainMm
    ) {}

    record IrrigationEventOutcome(
            UUID eventId,
            UUID fieldId,
            Instant startedAt,
            Instant endedAt,
            BigDecimal actualVolumeM3,
            String status,
            String notes
    ) {}

    record SmartPlanOutcome(
            UUID planId,
            UUID fieldId,
            Instant plannedStartAt,
            BigDecimal recommendedVolumeM3,
            String status,
            boolean isBlocked,
            String blockedReason
    ) {}

    List<FieldMetadata> getAccessibleFields(UUID userId, boolean isAdminOrOperator);

    Optional<FieldMetadata> getFieldMetadata(UUID fieldId);

    List<TelemetryObservation> getRecentMoistureObservations(UUID fieldId, Instant since);

    Optional<WaterBalance> getWaterBalance(UUID fieldId);

    List<UpcomingPlanInfo> getUpcomingPlans(UUID fieldId, Instant until);

    List<HydraulicAnomalyInfo> getActiveHydraulicAnomalies(UUID fieldId);

    List<SensorHealthSummary> getSensorHealthHistory(UUID fieldId, Instant since);

    WeatherContext getWeatherContext(UUID fieldId);

    // --- Outcome Matching Accessors for Milestone 08.8 ---

    List<TelemetryObservation> getMoistureObservationsInWindow(UUID fieldId, Instant windowStart, Instant windowEnd);

    List<IrrigationEventOutcome> getIrrigationEventsInWindow(UUID fieldId, Instant from, Instant to);

    List<SmartPlanOutcome> getSmartPlansInWindow(UUID fieldId, Instant from, Instant to);

    List<HydraulicAnomalyInfo> getHydraulicAnomaliesInWindow(UUID fieldId, Instant from, Instant to);

    BigDecimal getActualWaterConsumptionInWindow(UUID fieldId, Instant from, Instant to);
}
