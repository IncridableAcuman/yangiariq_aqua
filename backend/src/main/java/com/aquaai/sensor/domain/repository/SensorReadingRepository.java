package com.aquaai.sensor.domain.repository;

import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for time-series {@link SensorReading} telemetry observations.
 */
public interface SensorReadingRepository {

    SensorReading save(SensorReading reading);

    List<SensorReading> saveAll(List<SensorReading> readings);

    Optional<SensorReading> findLatestBySensorId(UUID sensorId);

    SensorReadingPage findBySensorId(UUID sensorId, Instant from, Instant to, int page, int size, String sortDirection);

    SensorReadingPage findByFieldId(UUID fieldId, Instant from, Instant to, int page, int size, String sortDirection);

    List<SensorReading> findLatestByFieldIdGroupedByType(UUID fieldId);

    List<SensorReading> findLatestSoilMoistureByFieldIdIn(java.util.Collection<UUID> fieldIds);

    int deleteByMeasuredAtBefore(Instant cutoff);
}
