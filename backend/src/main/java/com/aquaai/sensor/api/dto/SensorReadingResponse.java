package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Response representation of an individual telemetry observation")
public record SensorReadingResponse(
        UUID id,
        UUID sensorId,
        UUID fieldId,
        SensorType type,
        BigDecimal value,
        MeasurementUnit unit,
        String unitSymbol,
        ReadingQuality quality,
        String source,
        Instant measuredAt,
        Instant createdAt
) {
    public static SensorReadingResponse from(SensorReading reading) {
        if (reading == null) {
            return null;
        }
        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensorId(),
                reading.getFieldId(),
                reading.getType(),
                reading.getValue(),
                reading.getUnit(),
                reading.getUnit().getSymbol(),
                reading.getQuality(),
                reading.getSource(),
                reading.getMeasuredAt(),
                reading.getCreatedAt()
        );
    }
}
