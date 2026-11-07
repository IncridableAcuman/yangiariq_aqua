package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.service.TemporalTrendCalculator;
import com.aquaai.risk.domain.port.RiskSensorPort;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskSensorAdapter implements RiskSensorPort {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final TemporalTrendCalculator trendCalculator = new TemporalTrendCalculator();

    @Override
    public List<Sensor> getSensors(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        return sensorRepository.findByFieldId(fieldId);
    }

    @Override
    public List<SensorReading> getLatestReadings(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        return readingRepository.findLatestByFieldIdGroupedByType(fieldId);
    }

    @Override
    public Optional<TelemetryTrend> getMoistureTrend(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        Instant to = Instant.now();
        Instant from = to.minus(24, ChronoUnit.HOURS);
        SensorReadingPage page = readingRepository.findByFieldId(fieldId, from, to, 0, 100, "asc");
        if (page == null || page.content() == null || page.content().isEmpty()) {
            return Optional.empty();
        }
        List<SensorReading> moistureReadings = page.content().stream()
                .filter(r -> r.getType() == SensorType.SOIL_MOISTURE)
                .toList();

        if (moistureReadings.size() < 2) {
            return Optional.empty();
        }
        List<TemporalTrendCalculator.DataPoint> dataPoints = moistureReadings.stream()
                .map(r -> new TemporalTrendCalculator.DataPoint(r.getValue(), r.getMeasuredAt()))
                .toList();
        return Optional.of(trendCalculator.calculateTrend(dataPoints));
    }
}
