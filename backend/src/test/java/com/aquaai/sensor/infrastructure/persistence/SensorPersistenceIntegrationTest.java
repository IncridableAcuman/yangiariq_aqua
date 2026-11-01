package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SensorPersistenceIntegrationTest {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private UUID fieldId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .firstName("Persist")
                .lastName("Farmer")
                .fullName("Persist Farmer")
                .phoneNumber("+99890" + (System.currentTimeMillis() % 10000000L))
                .email("persist." + UUID.randomUUID() + "@aquaai.uz")
                .passwordHash("hashedTestingPassword123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        Field field = Field.create(
                user.getId(),
                "Persist Field",
                "Sinov maydoni",
                new BigDecimal("12.50"),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                new FieldLocation(new BigDecimal("41.341400"), new BigDecimal("60.609400")),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.60, 41.34),
                        CoordinatePoint.of(60.62, 41.34),
                        CoordinatePoint.of(60.62, 41.36),
                        CoordinatePoint.of(60.60, 41.36),
                        CoordinatePoint.of(60.60, 41.34)
                ))
        );
        Field savedField = fieldRepository.save(field);
        this.fieldId = savedField.getId();
    }

    @Test
    @Transactional
    @DisplayName("Sensor and time-series readings persist and retrieve accurately")
    void persistAndRetrieve_success() {
        String deviceId = "SN-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        Sensor sensor = Sensor.create(fieldId, "Soil Probe A", deviceId, SensorType.SOIL_MOISTURE, "SIMULATED", "Depth 30cm");
        Sensor savedSensor = sensorRepository.save(sensor);

        assertThat(savedSensor.getId()).isNotNull();
        assertThat(savedSensor.getVersion()).isNotNull();

        Instant t1 = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant t2 = Instant.now().minus(1, ChronoUnit.HOURS);

        SensorReading r1 = SensorReading.create(
                savedSensor.getId(),
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("48.50"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "SIMULATED",
                t1
        );
        SensorReading r2 = SensorReading.create(
                savedSensor.getId(),
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("49.20"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "SIMULATED",
                t2
        );

        sensorReadingRepository.save(r1);
        sensorReadingRepository.save(r2);

        // Verify latest reading query returns t2
        Optional<SensorReading> latestOpt = sensorReadingRepository.findLatestBySensorId(savedSensor.getId());
        assertThat(latestOpt).isPresent();
        assertThat(latestOpt.get().getValue()).isEqualByComparingTo(new BigDecimal("49.2000"));
        assertThat(latestOpt.get().getMeasuredAt()).isEqualTo(t2);

        // Verify paginated search
        SensorReadingPage page = sensorReadingRepository.findBySensorId(
                savedSensor.getId(),
                t1.minus(1, ChronoUnit.MINUTES),
                Instant.now(),
                0,
                10,
                "desc"
        );
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content().get(0).getValue()).isEqualByComparingTo(new BigDecimal("49.2000"));
    }

    @Test
    @Transactional
    @DisplayName("Foreign key constraint prevents inserting sensor with non-existent field_id")
    void foreignKeyConstraint_nonExistentField_fails() {
        UUID nonExistentField = UUID.randomUUID();
        Sensor orphan = Sensor.create(
                nonExistentField,
                "Orphan Sensor",
                "SN-ORPHAN-" + UUID.randomUUID().toString().substring(0, 6),
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                null
        );

        assertThatThrownBy(() -> sensorRepository.save(orphan))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    @DisplayName("Unique constraint on device_identifier prevents duplicate hardware serials")
    void uniqueConstraint_duplicateDeviceIdentifier_fails() {
        String sharedId = "SN-SHARED-SERIAL-01";
        Sensor s1 = Sensor.create(fieldId, "Sensor 1", sharedId, SensorType.SOIL_MOISTURE, "SIMULATED", null);
        sensorRepository.save(s1);

        Sensor s2 = Sensor.create(fieldId, "Sensor 2", sharedId, SensorType.WATER_FLOW, "SIMULATED", null);

        assertThatThrownBy(() -> sensorRepository.save(s2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    @DisplayName("Deleting sensor cascades deletion to attached readings")
    void cascadeDelete_sensorDeletesReadings() {
        String deviceId = "SN-CASCADE-" + UUID.randomUUID().toString().substring(0, 8);
        Sensor sensor = Sensor.create(fieldId, "Ephemeral Probe", deviceId, SensorType.SOIL_MOISTURE, "SIMULATED", null);
        Sensor savedSensor = sensorRepository.save(sensor);

        SensorReading reading = SensorReading.create(
                savedSensor.getId(),
                fieldId,
                SensorType.SOIL_MOISTURE,
                new BigDecimal("50.00"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "SIMULATED",
                Instant.now()
        );
        sensorReadingRepository.save(reading);

        assertThat(sensorReadingRepository.findLatestBySensorId(savedSensor.getId())).isPresent();

        // Delete sensor
        sensorRepository.deleteById(savedSensor.getId());
        entityManager.flush();
        entityManager.clear();

        // Readings must be gone
        assertThat(sensorReadingRepository.findLatestBySensorId(savedSensor.getId())).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("Efficient field summary query returns only latest reading per sensor type")
    void findLatestByFieldIdGroupedByType_returnsLatestPerType() {
        Sensor sMoisture = sensorRepository.save(Sensor.create(fieldId, "Moisture", "SN-M-" + UUID.randomUUID(), SensorType.SOIL_MOISTURE, "SIMULATED", null));
        Sensor sTemp = sensorRepository.save(Sensor.create(fieldId, "Temp", "SN-T-" + UUID.randomUUID(), SensorType.TEMPERATURE, "SIMULATED", null));

        Instant past = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant recent = Instant.now().minus(2, ChronoUnit.MINUTES);

        // Old readings
        sensorReadingRepository.save(SensorReading.create(sMoisture.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("45.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", past));
        sensorReadingRepository.save(SensorReading.create(sTemp.getId(), fieldId, SensorType.TEMPERATURE, new BigDecimal("20.0"), MeasurementUnit.CELSIUS, ReadingQuality.VALID, "TEST", past));

        // Newer readings
        sensorReadingRepository.save(SensorReading.create(sMoisture.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("55.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", recent));
        sensorReadingRepository.save(SensorReading.create(sTemp.getId(), fieldId, SensorType.TEMPERATURE, new BigDecimal("25.0"), MeasurementUnit.CELSIUS, ReadingQuality.VALID, "TEST", recent));

        List<SensorReading> latestGrouped = sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);

        assertThat(latestGrouped).hasSize(2);
        SensorReading latestMoisture = latestGrouped.stream().filter(r -> r.getType() == SensorType.SOIL_MOISTURE).findFirst().orElseThrow();
        SensorReading latestTemp = latestGrouped.stream().filter(r -> r.getType() == SensorType.TEMPERATURE).findFirst().orElseThrow();

        assertThat(latestMoisture.getValue()).isEqualByComparingTo(new BigDecimal("55.0000"));
        assertThat(latestTemp.getValue()).isEqualByComparingTo(new BigDecimal("25.0000"));
    }
}
