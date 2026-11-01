package com.aquaai.anomaly.application.service;

import com.aquaai.anomaly.application.port.AnomalyNotificationPort;
import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnomalyEvaluationServiceIntegrationTest {

    @Autowired
    private AnomalyEvaluationService evaluationService;

    @Autowired
    private AnomalyRepository anomalyRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AnomalyNotificationPort notificationPort;

    private User createTestUser(String prefix) {
        long id = System.nanoTime() % 10000000;
        return userRepository.save(User.builder()
                .firstName("Test")
                .lastName("Farmer")
                .fullName("Test Farmer")
                .phoneNumber("+99890" + String.format("%07d", id))
                .email(prefix + "." + id + "@yangiariq.uz")
                .passwordHash("hashed")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
    }

    @Test
    @DisplayName("Deduplication: consecutive critical readings update active anomaly observation count instead of duplicating")
    void shouldDeduplicateActiveAnomalies() {
        User owner = createTestUser("dedup");
        UUID ownerId = owner.getId();
        Field field = fieldRepository.save(Field.create(ownerId, "Deduplication Parcel", "Desc", BigDecimal.valueOf(10.0), CropType.COTTON, SoilType.LOAM, IrrigationMethod.DRIP, null, null));
        Sensor sensor = sensorRepository.save(Sensor.create(field.getId(), "Moisture Probe", "SN-DEDUP-01", SensorType.SOIL_MOISTURE, "SIMULATED", null));

        // Reading 1: Critical deficit (11.5%)
        SensorReading reading1 = sensorReadingRepository.save(SensorReading.create(
                sensor.getId(), field.getId(), SensorType.SOIL_MOISTURE, BigDecimal.valueOf(11.5), MeasurementUnit.PERCENT, ReadingQuality.VALID, "PHYSICAL_GATEWAY", Instant.now()
        ));

        List<Anomaly> firstRun = evaluationService.evaluateReading(reading1);
        assertThat(firstRun).hasSize(1);
        Anomaly initial = firstRun.get(0);
        assertThat(initial.getObservationCount()).isEqualTo(1);
        assertThat(initial.getStatus()).isEqualTo(AnomalyStatus.DETECTED);
        verify(notificationPort, times(1)).sendAlert(any());

        // Reading 2: Still critical deficit (11.0%) 5 minutes later
        SensorReading reading2 = sensorReadingRepository.save(SensorReading.create(
                sensor.getId(), field.getId(), SensorType.SOIL_MOISTURE, BigDecimal.valueOf(11.0), MeasurementUnit.PERCENT, ReadingQuality.VALID, "PHYSICAL_GATEWAY", Instant.now().plusSeconds(300)
        ));

        List<Anomaly> secondRun = evaluationService.evaluateReading(reading2);
        assertThat(secondRun).hasSize(1);
        Anomaly updated = secondRun.get(0);

        // Crucial invariant: same anomaly ID, incremented observationCount
        assertThat(updated.getId()).isEqualTo(initial.getId());
        assertThat(updated.getObservationCount()).isEqualTo(2);
        assertThat(updated.getDetectedValue()).isEqualByComparingTo(BigDecimal.valueOf(11.0));

        // Cooldown: notification should NOT be dispatched twice within cooldown window (30 mins)
        verify(notificationPort, times(1)).sendAlert(any());
    }

    @Test
    @DisplayName("Refinement #5: Simulated telemetry creates anomaly marked isSimulated=true")
    void shouldMarkSimulatedTelemetryAnomalies() {
        User owner = createTestUser("sim");
        UUID ownerId = owner.getId();
        Field field = fieldRepository.save(Field.create(ownerId, "Simulation Parcel", "Desc", BigDecimal.valueOf(5.0), CropType.WHEAT, SoilType.CLAY, IrrigationMethod.CANAL, null, null));
        Sensor sensor = sensorRepository.save(Sensor.create(field.getId(), "Simulated Flow", "SN-SIM-01", SensorType.WATER_FLOW, "SIMULATED", null));

        // Simulated flow spike
        SensorReading simReading = sensorReadingRepository.save(SensorReading.create(
                sensor.getId(), field.getId(), SensorType.WATER_FLOW, BigDecimal.valueOf(58.0), MeasurementUnit.CUBIC_METER_PER_HOUR, ReadingQuality.VALID, "SIMULATED", Instant.now()
        ));

        List<Anomaly> detected = evaluationService.evaluateReading(simReading);
        assertThat(detected).isNotEmpty();
        Anomaly anomaly = detected.get(0);
        assertThat(anomaly.isSimulated()).isTrue();
    }
}
