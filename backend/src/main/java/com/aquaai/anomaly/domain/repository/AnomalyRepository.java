package com.aquaai.anomaly.domain.repository;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.SensorType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for anomaly persistence and lifecycle querying.
 */
public interface AnomalyRepository {

    Anomaly save(Anomaly anomaly);

    Optional<Anomaly> findById(UUID id);

    /**
     * Looks up an existing active (non-terminal) anomaly matching the deduplication key.
     */
    Optional<Anomaly> findActiveByFieldAndSensorAndType(
            UUID fieldId,
            UUID sensorId,
            AnomalyType anomalyType
    );

    List<Anomaly> findAll(
            UUID fieldId,
            UUID sensorId,
            AnomalyStatus status,
            AnomalySeverity severity,
            AnomalyType anomalyType,
            SensorType sensorType,
            Instant from,
            Instant to,
            int page,
            int size
    );

    long countAll(
            UUID fieldId,
            UUID sensorId,
            AnomalyStatus status,
            AnomalySeverity severity,
            AnomalyType anomalyType,
            SensorType sensorType,
            Instant from,
            Instant to
    );

    long countActiveByField(UUID fieldId);

    List<Anomaly> findActiveByFieldIdIn(java.util.Collection<UUID> fieldIds);

    long countByStatus(AnomalyStatus status);

    long countBySeverity(AnomalySeverity severity);
}
