package com.aquaai.network.infrastructure.adapter;

import com.aquaai.network.domain.port.NetworkSensorPort;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NetworkSensorAdapter implements NetworkSensorPort {

    private final SensorReadingRepository readingRepository;

    @Override
    public Optional<BigDecimal> getLatestPressureBar(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        List<SensorReading> readings = readingRepository.findLatestByFieldIdGroupedByType(fieldId);
        if (readings == null) return Optional.empty();
        return readings.stream()
                .filter(r -> r.getType() == SensorType.PRESSURE)
                .map(SensorReading::getValue)
                .findFirst();
    }

    @Override
    public Optional<BigDecimal> getLatestFlowM3H(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        List<SensorReading> readings = readingRepository.findLatestByFieldIdGroupedByType(fieldId);
        if (readings == null) return Optional.empty();
        return readings.stream()
                .filter(r -> r.getType() == SensorType.WATER_FLOW)
                .map(SensorReading::getValue)
                .findFirst();
    }
}
