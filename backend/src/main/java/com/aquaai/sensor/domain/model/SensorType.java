package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Types of sensors deployed across agricultural fields and secondary distribution infrastructure in Yangiariq.
 */
@Getter
@RequiredArgsConstructor
public enum SensorType {

    SOIL_MOISTURE(
            MeasurementUnit.PERCENT,
            Set.of(MeasurementUnit.PERCENT),
            new BigDecimal("0.0"),
            new BigDecimal("100.0")
    ),

    WATER_FLOW(
            MeasurementUnit.CUBIC_METER_PER_HOUR,
            Set.of(MeasurementUnit.CUBIC_METER_PER_HOUR, MeasurementUnit.LITERS_PER_SECOND),
            BigDecimal.ZERO,
            new BigDecimal("10000.0")
    ),

    TEMPERATURE(
            MeasurementUnit.CELSIUS,
            Set.of(MeasurementUnit.CELSIUS),
            new BigDecimal("-40.0"),
            new BigDecimal("60.0")
    ),

    HUMIDITY(
            MeasurementUnit.PERCENT,
            Set.of(MeasurementUnit.PERCENT),
            new BigDecimal("0.0"),
            new BigDecimal("100.0")
    ),

    WATER_LEVEL(
            MeasurementUnit.METERS,
            Set.of(MeasurementUnit.METERS, MeasurementUnit.PERCENT),
            BigDecimal.ZERO,
            new BigDecimal("50.0")
    ),

    PRESSURE(
            MeasurementUnit.BAR,
            Set.of(MeasurementUnit.BAR, MeasurementUnit.MILLIBAR),
            BigDecimal.ZERO,
            new BigDecimal("25.0")
    ),

    RAINFALL(
            MeasurementUnit.MILLIMETERS,
            Set.of(MeasurementUnit.MILLIMETERS),
            BigDecimal.ZERO,
            new BigDecimal("500.0")
    ),

    SALINITY(
            MeasurementUnit.DECISIEMENS_PER_METER,
            Set.of(MeasurementUnit.DECISIEMENS_PER_METER),
            BigDecimal.ZERO,
            new BigDecimal("50.0")
    );

    private final MeasurementUnit defaultUnit;
    private final Set<MeasurementUnit> allowedUnits;
    private final BigDecimal minPhysicalValue;
    private final BigDecimal maxPhysicalValue;

    public boolean isUnitCompatible(MeasurementUnit unit) {
        return unit != null && this.allowedUnits.contains(unit);
    }

    public void validateValue(BigDecimal value, MeasurementUnit unit) {
        if (value == null) {
            throw new BusinessRuleException("Measurement value must not be null", "NULL_MEASUREMENT_VALUE");
        }
        if (unit == null) {
            throw new BusinessRuleException("Measurement unit must not be null", "NULL_MEASUREMENT_UNIT");
        }
        if (!isUnitCompatible(unit)) {
            throw new BusinessRuleException(
                    String.format("Unit '%s' is physically incompatible with sensor type %s", unit, this),
                    "INCOMPATIBLE_SENSOR_UNIT"
            );
        }

        // Special handling for WATER_LEVEL when measured in PERCENT
        if (this == WATER_LEVEL && unit == MeasurementUnit.PERCENT) {
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100.0")) > 0) {
                throw new BusinessRuleException(
                        String.format("Water level percentage must be between 0%% and 100%%, got: %s", value),
                        "OUT_OF_BOUNDS_MEASUREMENT"
                );
            }
            return;
        }

        // Special handling for PRESSURE when measured in MILLIBAR
        if (this == PRESSURE && unit == MeasurementUnit.MILLIBAR) {
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("25000.0")) > 0) {
                throw new BusinessRuleException(
                        String.format("Pressure in millibar must be between 0 and 25000 mbar, got: %s", value),
                        "OUT_OF_BOUNDS_MEASUREMENT"
                );
            }
            return;
        }

        if (minPhysicalValue != null && value.compareTo(minPhysicalValue) < 0) {
            throw new BusinessRuleException(
                    String.format("Measurement value %s %s is below physical minimum %s for %s",
                            value, unit, minPhysicalValue, this),
                    "OUT_OF_BOUNDS_MEASUREMENT"
            );
        }
        if (maxPhysicalValue != null && value.compareTo(maxPhysicalValue) > 0) {
            throw new BusinessRuleException(
                    String.format("Measurement value %s %s exceeds physical maximum %s for %s",
                            value, unit, maxPhysicalValue, this),
                    "OUT_OF_BOUNDS_MEASUREMENT"
            );
        }
    }
}
