package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorReadingUnitTest {

    private final UUID sensorId = UUID.randomUUID();
    private final UUID fieldId = UUID.randomUUID();

    @Test
    @DisplayName("SensorReading creates successfully with valid value and default unit")
    void createSensorReading_valid_success() {
        SensorReading reading = SensorReading.create(
                sensorId,
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("54.2500"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "SIMULATED",
                Instant.now()
        );

        assertThat(reading.getId()).isNotNull();
        assertThat(reading.getSensorId()).isEqualTo(sensorId);
        assertThat(reading.getFieldId()).isEqualTo(fieldId);
        assertThat(reading.getType()).isEqualTo(SensorType.SOIL_MOISTURE);
        assertThat(reading.getValue()).isEqualByComparingTo(new BigDecimal("54.2500"));
        assertThat(reading.getUnit()).isEqualTo(MeasurementUnit.PERCENT);
        assertThat(reading.getQuality()).isEqualTo(ReadingQuality.VALID);
        assertThat(reading.getSource()).isEqualTo("SIMULATED");
        assertThat(reading.getMeasuredAt()).isNotNull();
    }

    @Test
    @DisplayName("SensorReading rejects physically incompatible measurement unit")
    void createSensorReading_incompatibleUnit_throwsException() {
        assertThatThrownBy(() -> SensorReading.create(
                sensorId,
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("50.0"),
                MeasurementUnit.CUBIC_METER_PER_HOUR, // Incompatible!
                ReadingQuality.VALID,
                "TEST",
                Instant.now()
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("physically incompatible with sensor type");
    }

    @Test
    @DisplayName("SensorReading rejects out-of-bounds physical measurement values")
    void createSensorReading_outOfBounds_throwsException() {
        // Soil moisture cannot exceed 100%
        assertThatThrownBy(() -> SensorReading.create(
                sensorId,
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("105.0"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "TEST",
                Instant.now()
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("exceeds physical maximum");

        // Negative water flow is physically impossible
        assertThatThrownBy(() -> SensorReading.create(
                sensorId,
                fieldId,
                SensorType.WATER_FLOW,
                new BigDecimal("-2.5"),
                MeasurementUnit.CUBIC_METER_PER_HOUR,
                ReadingQuality.VALID,
                "TEST",
                Instant.now()
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("below physical minimum");

        // Temperature outside agricultural extremes
        assertThatThrownBy(() -> SensorReading.create(
                sensorId,
                fieldId,
                SensorType.TEMPERATURE,
                new BigDecimal("75.0"),
                MeasurementUnit.CELSIUS,
                ReadingQuality.VALID,
                "TEST",
                Instant.now()
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("exceeds physical maximum");
    }

    @Test
    @DisplayName("SensorReading rejects measurement timestamps in the future beyond acceptable clock skew")
    void createSensorReading_futureTimestamp_throwsException() {
        Instant farFuture = Instant.now().plus(1, ChronoUnit.HOURS);

        assertThatThrownBy(() -> SensorReading.create(
                sensorId,
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("45.0"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "TEST",
                farFuture
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("timestamp cannot be in the future");
    }

    @Test
    @DisplayName("SensorReading accepts legitimate slight clock skew within 5 minutes")
    void createSensorReading_slightClockSkew_accepted() {
        Instant slightFuture = Instant.now().plus(2, ChronoUnit.MINUTES);

        SensorReading reading = SensorReading.create(
                sensorId,
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("45.0"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "TEST",
                slightFuture
        );

        assertThat(reading).isNotNull();
        assertThat(reading.getMeasuredAt()).isEqualTo(slightFuture);
    }
}
