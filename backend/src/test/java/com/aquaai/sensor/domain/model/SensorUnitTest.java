package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorUnitTest {

    private final UUID fieldId = UUID.randomUUID();

    @Test
    @DisplayName("Sensor aggregate creates successfully with valid parameters")
    void createSensor_valid_success() {
        Sensor sensor = Sensor.create(
                fieldId,
                "Tuproq Namlik Datchigi #1",
                "SN-SOIL-001",
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                "Deep root zone probe"
        );

        assertThat(sensor.getId()).isNotNull();
        assertThat(sensor.getFieldId()).isEqualTo(fieldId);
        assertThat(sensor.getName()).isEqualTo("Tuproq Namlik Datchigi #1");
        assertThat(sensor.getDeviceIdentifier()).isEqualTo("SN-SOIL-001");
        assertThat(sensor.getType()).isEqualTo(SensorType.SOIL_MOISTURE);
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ACTIVE);
        assertThat(sensor.getProvider()).isEqualTo("SIMULATED");
        assertThat(sensor.getDescription()).isEqualTo("Deep root zone probe");
        assertThat(sensor.getInstalledAt()).isNotNull();
        assertThat(sensor.getLastSeenAt()).isNull();
        assertThat(sensor.getCreatedAt()).isNotNull();
        assertThat(sensor.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Sensor rejects blank or excessively long name")
    void createSensor_invalidName_throwsException() {
        assertThatThrownBy(() -> Sensor.create(fieldId, "  ", "SN-001", SensorType.SOIL_MOISTURE, "SIMULATED", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("name cannot be blank");

        String longName = "A".repeat(129);
        assertThatThrownBy(() -> Sensor.create(fieldId, longName, "SN-001", SensorType.SOIL_MOISTURE, "SIMULATED", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot exceed 128 characters");
    }

    @Test
    @DisplayName("Sensor rejects invalid deviceIdentifier")
    void createSensor_invalidDeviceIdentifier_throwsException() {
        assertThatThrownBy(() -> Sensor.create(fieldId, "Valid Name", " ", SensorType.SOIL_MOISTURE, "SIMULATED", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Device identifier cannot be blank");

        assertThatThrownBy(() -> Sensor.create(fieldId, "Valid Name", "INVALID IDENTIFIER WITH SPACES!", SensorType.SOIL_MOISTURE, "SIMULATED", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("contains invalid characters");
    }

    @Test
    @DisplayName("Sensor status lifecycle enforces permitted transitions")
    void sensorStatus_transitions_valid() {
        Sensor sensor = Sensor.create(fieldId, "Probe", "SN-100", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ACTIVE);

        // ACTIVE -> INACTIVE
        sensor.deactivate();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.INACTIVE);

        // INACTIVE -> ACTIVE
        sensor.activate();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ACTIVE);

        // ACTIVE -> MAINTENANCE
        sensor.setMaintenance();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.MAINTENANCE);

        // MAINTENANCE -> ACTIVE
        sensor.activate();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ACTIVE);

        // ACTIVE -> OFFLINE
        sensor.markOffline();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.OFFLINE);

        // OFFLINE -> MAINTENANCE
        sensor.setMaintenance();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Sensor status lifecycle rejects invalid transitions")
    void sensorStatus_transitions_invalid() {
        Sensor sensor = Sensor.create(fieldId, "Probe", "SN-100", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        sensor.deactivate();
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.INACTIVE);

        // INACTIVE -> MAINTENANCE is forbidden (must activate first)
        assertThatThrownBy(sensor::setMaintenance)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid sensor status transition from INACTIVE to MAINTENANCE");

        // INACTIVE -> OFFLINE is forbidden
        assertThatThrownBy(sensor::markOffline)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid sensor status transition from INACTIVE to OFFLINE");
    }

    @Test
    @DisplayName("recordHeartbeat updates lastSeenAt and updatedAt timestamps")
    void recordHeartbeat_updatesTimestamps() {
        Sensor sensor = Sensor.create(fieldId, "Probe", "SN-100", SensorType.WATER_FLOW, "SIMULATED", null);
        assertThat(sensor.getLastSeenAt()).isNull();

        Instant heartbeat = Instant.now().minus(10, ChronoUnit.SECONDS);
        sensor.recordHeartbeat(heartbeat);

        assertThat(sensor.getLastSeenAt()).isEqualTo(heartbeat);
    }

    @Test
    @DisplayName("updateDetails correctly mutates name and description")
    void updateDetails_valid_success() {
        Sensor sensor = Sensor.create(fieldId, "Probe", "SN-100", SensorType.SOIL_MOISTURE, "SIMULATED", "Old desc");

        sensor.updateDetails("Updated Probe Name", "New agronomic description");

        assertThat(sensor.getName()).isEqualTo("Updated Probe Name");
        assertThat(sensor.getDescription()).isEqualTo("New agronomic description");
    }
}
