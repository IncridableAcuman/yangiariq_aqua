package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.intelligence.domain.port.IntelligenceTelemetryPort;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntelligenceTelemetryAdapter implements IntelligenceTelemetryPort {

    private final SensorReadingRepository sensorReadingRepository;

    @Override
    public List<SensorReading> getLatestReadings(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        return sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
    }

    @Override
    public List<SensorReading> getRecentReadings(UUID fieldId, SensorType type, Instant from, Instant to) {
        if (fieldId == null) return Collections.emptyList();
        SensorReadingPage page = sensorReadingRepository.findByFieldId(fieldId, from, to, 0, 100, "asc");
        if (page == null || page.content() == null) {
            return Collections.emptyList();
        }
        if (type == null) {
            return page.content();
        }
        return page.content().stream()
                .filter(r -> r.getType() == type)
                .toList();
    }
}
