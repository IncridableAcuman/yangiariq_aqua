package com.aquaai.intelligence.application.dto;

import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.intelligence.domain.model.TelemetrySignal;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TelemetrySignalResponse(
        UUID fieldId,
        UUID sensorId,
        SensorType type,
        BigDecimal value,
        MeasurementUnit unit,
        ReadingQuality quality,
        Instant measuredAt,
        TelemetryFreshness freshness,
        boolean isAuthoritative
) {
    public static TelemetrySignalResponse fromDomain(TelemetrySignal domain) {
        if (domain == null) return null;
        return new TelemetrySignalResponse(
                domain.getFieldId(),
                domain.getSensorId(),
                domain.getType(),
                domain.getValue(),
                domain.getUnit(),
                domain.getQuality(),
                domain.getMeasuredAt(),
                domain.getFreshness(),
                domain.isAuthoritative()
        );
    }
}
