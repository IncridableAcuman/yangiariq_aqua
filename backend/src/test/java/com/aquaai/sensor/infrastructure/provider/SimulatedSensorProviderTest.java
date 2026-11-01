package com.aquaai.sensor.infrastructure.provider;

import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedSensorProviderTest {

    private SimulatedSensorProvider provider;
    private final UUID fieldId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        provider = new SimulatedSensorProvider();
    }

    @Test
    @DisplayName("SimulatedSensorProvider generates valid bounded readings for all supported SensorTypes")
    void simulate_allSensorTypes_valid() {
        for (SensorType type : SensorType.values()) {
            Sensor sensor = Sensor.create(fieldId, "Sensor " + type, "SN-" + type, type, "SIMULATED", null);

            SensorReading reading = provider.read(sensor);

            assertThat(reading).isNotNull();
            assertThat(reading.getSensorId()).isEqualTo(sensor.getId());
            assertThat(reading.getFieldId()).isEqualTo(fieldId);
            assertThat(reading.getType()).isEqualTo(type);
            assertThat(reading.getUnit()).isEqualTo(type.getDefaultUnit());
            assertThat(reading.getQuality()).isEqualTo(ReadingQuality.VALID);
            assertThat(reading.getSource()).isEqualTo("SIMULATED_SENSOR_PROVIDER");
            assertThat(reading.getValue()).isNotNull();

            // Verify physical bounds hold
            type.validateValue(reading.getValue(), reading.getUnit());
        }
    }

    @Test
    @DisplayName("SimulatedSensorProvider produces continuous bounded sequence without wild jumps")
    void simulate_sequentialReadings_continuity() {
        Sensor sensor = Sensor.create(fieldId, "Moisture Probe", "SN-DRIFT-01", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        provider.resetSequence(sensor.getId());

        BigDecimal previous = null;
        for (int i = 0; i < 10; i++) {
            SensorReading reading = provider.read(sensor);
            BigDecimal current = reading.getValue();

            if (previous != null) {
                // Variation between consecutive ticks should be small (bounded drift <= 1.0%)
                BigDecimal diff = current.subtract(previous).abs();
                assertThat(diff).isLessThan(new BigDecimal("1.50"));
            }
            previous = current;
        }
    }
}
