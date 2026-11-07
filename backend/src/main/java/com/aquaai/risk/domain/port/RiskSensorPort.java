package com.aquaai.risk.domain.port;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskSensorPort {

    List<Sensor> getSensors(UUID fieldId);

    List<SensorReading> getLatestReadings(UUID fieldId);

    Optional<TelemetryTrend> getMoistureTrend(UUID fieldId);
}
