package com.aquaai.irrigation.domain.port;

import com.aquaai.irrigation.domain.model.DataQualityGate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for retrieving recent telemetry readings for a parcel.
 */
public interface TelemetryDataPort {

    record ParcelTelemetryData(
            UUID fieldId,
            BigDecimal soilMoisturePercent,
            BigDecimal waterFlowM3H,
            BigDecimal pressureBar,
            BigDecimal ambientTemperatureCelsius,
            BigDecimal ambientHumidityPercent,
            BigDecimal salinityDsM,
            DataQualityGate qualityGate
    ) {}

    Optional<ParcelTelemetryData> getLatestTelemetry(UUID fieldId);
}
