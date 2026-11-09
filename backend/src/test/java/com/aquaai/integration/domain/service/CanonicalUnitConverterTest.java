package com.aquaai.integration.domain.service;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CanonicalUnitConverterTest {

    private final CanonicalUnitConverter converter = new CanonicalUnitConverter();

    @Test
    @DisplayName("Should convert liters per second to cubic meters per hour")
    void shouldConvertLitersPerSecondToCubicMetersPerHour() {
        // 10 L/s * 3.6 = 36 m³/h
        var result = converter.convertToCanonical(SensorType.WATER_FLOW, BigDecimal.valueOf(10.0), "L/s");
        assertThat(result.canonicalUnit()).isEqualTo(MeasurementUnit.CUBIC_METER_PER_HOUR);
        assertThat(result.normalizedValue()).isEqualByComparingTo(BigDecimal.valueOf(36.0));
    }

    @Test
    @DisplayName("Should keep cubic meters per hour unchanged")
    void shouldKeepCubicMetersPerHourUnchanged() {
        var result = converter.convertToCanonical(SensorType.WATER_FLOW, BigDecimal.valueOf(42.5), "m³/h");
        assertThat(result.canonicalUnit()).isEqualTo(MeasurementUnit.CUBIC_METER_PER_HOUR);
        assertThat(result.normalizedValue()).isEqualByComparingTo(BigDecimal.valueOf(42.5));
    }

    @Test
    @DisplayName("Should convert millibar to bar")
    void shouldConvertMillibarToBar() {
        // 2500 mbar = 2.5 bar
        var result = converter.convertToCanonical(SensorType.PRESSURE, BigDecimal.valueOf(2500.0), "mbar");
        assertThat(result.canonicalUnit()).isEqualTo(MeasurementUnit.BAR);
        assertThat(result.normalizedValue()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }

    @Test
    @DisplayName("Should convert kilopascal to bar")
    void shouldConvertKpaToBar() {
        // 200 kPa = 2.0 bar
        var result = converter.convertToCanonical(SensorType.PRESSURE, BigDecimal.valueOf(200.0), "kPa");
        assertThat(result.canonicalUnit()).isEqualTo(MeasurementUnit.BAR);
        assertThat(result.normalizedValue()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test
    @DisplayName("Should pass soil moisture percentage directly")
    void shouldPassSoilMoistureDirectly() {
        var result = converter.convertToCanonical(SensorType.SOIL_MOISTURE, BigDecimal.valueOf(45.2), "%");
        assertThat(result.canonicalUnit()).isEqualTo(MeasurementUnit.PERCENT);
        assertThat(result.normalizedValue()).isEqualByComparingTo(BigDecimal.valueOf(45.2));
    }

    @Test
    @DisplayName("Should reject null sensor type or value")
    void shouldRejectNulls() {
        assertThatThrownBy(() -> converter.convertToCanonical(null, BigDecimal.TEN, "%"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> converter.convertToCanonical(SensorType.SOIL_MOISTURE, null, "%"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> converter.convertToCanonical(SensorType.SOIL_MOISTURE, BigDecimal.TEN, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
