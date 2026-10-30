package com.aquaai.sensor.domain;

import java.util.List;
import java.util.UUID;

public interface SensorProvider {

    String getProviderType();

    SensorReading getLatestReading(UUID sensorId);

    List<SensorReading> getReadingsForField(UUID fieldId);

    boolean isAvailable();
}
