package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.DataQualityGate;
import com.aquaai.irrigation.domain.port.TelemetryDataPort;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TelemetryDataAdapter implements TelemetryDataPort {

    private final SensorReadingRepository sensorReadingRepository;

    @Override
    public Optional<ParcelTelemetryData> getLatestTelemetry(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }

        List<SensorReading> readings = sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
        if (readings.isEmpty()) {
            return Optional.of(new ParcelTelemetryData(
                    fieldId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    DataQualityGate.INSUFFICIENT
            ));
        }

        Map<SensorType, SensorReading> byType = new HashMap<>();
        for (SensorReading r : readings) {
            byType.put(r.getType(), r);
        }

        SensorReading moistureReading = byType.get(SensorType.SOIL_MOISTURE);
        SensorReading flowReading = byType.get(SensorType.WATER_FLOW);
        SensorReading pressureReading = byType.get(SensorType.PRESSURE);
        SensorReading tempReading = byType.get(SensorType.TEMPERATURE);
        SensorReading humidityReading = byType.get(SensorType.HUMIDITY);
        SensorReading salinityReading = byType.get(SensorType.SALINITY);

        BigDecimal moisture = moistureReading != null ? moistureReading.getValue() : null;
        BigDecimal flow = flowReading != null ? flowReading.getValue() : null;
        BigDecimal pressure = pressureReading != null ? pressureReading.getValue() : null;
        BigDecimal temp = tempReading != null ? tempReading.getValue() : null;
        BigDecimal humidity = humidityReading != null ? humidityReading.getValue() : null;
        BigDecimal salinity = salinityReading != null ? salinityReading.getValue() : null;

        DataQualityGate qualityGate = DataQualityGate.SUFFICIENT;
        if (moistureReading != null) {
            if (moistureReading.getQuality() == ReadingQuality.SUSPECT) {
                qualityGate = DataQualityGate.PARTIAL;
            } else if (moistureReading.getQuality() == ReadingQuality.INVALID) {
                qualityGate = DataQualityGate.INSUFFICIENT;
            }
        } else {
            qualityGate = DataQualityGate.PARTIAL;
        }

        return Optional.of(new ParcelTelemetryData(
                fieldId,
                moisture,
                flow,
                pressure,
                temp,
                humidity,
                salinity,
                qualityGate
        ));
    }
}
