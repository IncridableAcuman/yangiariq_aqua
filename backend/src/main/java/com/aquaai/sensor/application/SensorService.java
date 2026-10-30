package com.aquaai.sensor.application;

import com.aquaai.sensor.domain.SensorProvider;
import com.aquaai.sensor.domain.SensorReading;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorProvider sensorProvider;

    public SensorReading getReading(UUID sensorId) {
        return sensorProvider.getLatestReading(sensorId);
    }

    public List<SensorReading> getFieldReadings(UUID fieldId) {
        return sensorProvider.getReadingsForField(fieldId);
    }
}
