package com.aquaai.integration.domain.service;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Normalizes incoming raw telemetry units into canonical system units.
 */
public class CanonicalUnitConverter {

    public static final int CANONICAL_SCALE = 4;

    public record ConversionResult(
            BigDecimal normalizedValue,
            MeasurementUnit canonicalUnit
    ) {}

    /**
     * Converts a raw value and unit string to the canonical unit for the specified sensor type.
     */
    public ConversionResult convertToCanonical(SensorType type, BigDecimal rawValue, String rawUnitStr) {
        if (type == null) {
            throw new IllegalArgumentException("Sensor type must not be null");
        }
        if (rawValue == null) {
            throw new IllegalArgumentException("Raw value must not be null");
        }
        if (rawUnitStr == null || rawUnitStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Raw unit must not be blank");
        }

        String unitNorm = rawUnitStr.trim().toLowerCase();
        MeasurementUnit canonical = type.getDefaultUnit();

        BigDecimal normalized = switch (type) {
            case WATER_FLOW -> {
                if (unitNorm.equals("l/s") || unitNorm.equals("liters_per_second") || unitNorm.equals("litres/sec")) {
                    // 1 L/s = 3.6 m³/h
                    yield rawValue.multiply(BigDecimal.valueOf(3.6));
                } else if (unitNorm.equals("m³/h") || unitNorm.equals("m3/h") || unitNorm.equals("cubic_meter_per_hour")) {
                    yield rawValue;
                } else {
                    yield rawValue;
                }
            }
            case PRESSURE -> {
                if (unitNorm.equals("mbar") || unitNorm.equals("millibar")) {
                    // 1000 mbar = 1 bar
                    yield rawValue.divide(BigDecimal.valueOf(1000.0), CANONICAL_SCALE, RoundingMode.HALF_UP);
                } else if (unitNorm.equals("kpa") || unitNorm.equals("kilopascal")) {
                    // 100 kPa = 1 bar
                    yield rawValue.divide(BigDecimal.valueOf(100.0), CANONICAL_SCALE, RoundingMode.HALF_UP);
                } else if (unitNorm.equals("bar")) {
                    yield rawValue;
                } else {
                    yield rawValue;
                }
            }
            default -> rawValue;
        };

        return new ConversionResult(
                normalized.setScale(CANONICAL_SCALE, RoundingMode.HALF_UP),
                canonical
        );
    }
}
