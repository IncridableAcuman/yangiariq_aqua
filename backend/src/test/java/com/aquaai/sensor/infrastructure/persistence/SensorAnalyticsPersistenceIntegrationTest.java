package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.DataQualityMetrics;
import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SensorAnalyticsPersistenceIntegrationTest {

    @Autowired
    private SensorAnalyticsRepository sensorAnalyticsRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID fieldId;
    private Sensor testSensor;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .firstName("Analytics")
                .lastName("Farmer")
                .fullName("Analytics Farmer")
                .phoneNumber("+99890" + (System.currentTimeMillis() % 10000000L))
                .email("analytics." + UUID.randomUUID() + "@aquaai.uz")
                .passwordHash("hashedTestingPassword123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        Field field = Field.create(
                user.getId(),
                "Analytics Field",
                "Sinov maydoni",
                new BigDecimal("15.00"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
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

        this.testSensor = sensorRepository.save(Sensor.create(
                fieldId,
                "Moisture Sensor 01",
                "SN-TEST-ANALYTICS-" + UUID.randomUUID().toString().substring(0, 8),
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                "Root depth 30cm"
        ));
    }

    @Test
    @DisplayName("aggregateBySensor correctly computes hourly buckets, min, max, avg, and first/last values")
    void aggregateBySensor_computesBucketsCorrectly() {
        Instant baseTime = Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        // Window 1 (baseTime to baseTime + 1h): 3 readings: 40.0, 45.0, 50.0
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("40.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(10, ChronoUnit.MINUTES)
        ));
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("45.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(20, ChronoUnit.MINUTES)
        ));
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("50.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(40, ChronoUnit.MINUTES)
        ));

        // Window 2 (baseTime + 1h to baseTime + 2h): 2 readings: 60.0, 70.0
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("60.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(70, ChronoUnit.MINUTES)
        ));
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("70.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(90, ChronoUnit.MINUTES)
        ));

        entityManager.flush();
        entityManager.clear();

        List<TelemetryAggregate> aggregates = sensorAnalyticsRepository.aggregateBySensor(
                testSensor.getId(),
                baseTime.minus(5, ChronoUnit.MINUTES),
                baseTime.plus(2, ChronoUnit.HOURS),
                AggregationInterval.HOURLY,
                ReadingQuality.VALID
        );

        assertThat(aggregates).hasSize(2);

        // Window 1 verification: avg = (40+45+50)/3 = 45.0, min = 40.0, max = 50.0
        TelemetryAggregate b1 = aggregates.get(0);
        assertThat(b1.readingCount()).isEqualTo(3);
        assertThat(b1.minimum()).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(b1.maximum()).isEqualByComparingTo(new BigDecimal("50.0000"));
        assertThat(b1.average()).isEqualByComparingTo(new BigDecimal("45.0000"));
        assertThat(b1.firstReadingValue()).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(b1.lastReadingValue()).isEqualByComparingTo(new BigDecimal("50.0000"));

        // Window 2 verification: avg = (60+70)/2 = 65.0, min = 60.0, max = 70.0
        TelemetryAggregate b2 = aggregates.get(1);
        assertThat(b2.readingCount()).isEqualTo(2);
        assertThat(b2.minimum()).isEqualByComparingTo(new BigDecimal("60.0000"));
        assertThat(b2.maximum()).isEqualByComparingTo(new BigDecimal("70.0000"));
        assertThat(b2.average()).isEqualByComparingTo(new BigDecimal("65.0000"));
    }

    @Test
    @DisplayName("Quality filtering excludes INVALID readings from statistical aggregation")
    void qualityFilter_excludesInvalidReadings() {
        Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        // 1 VALID reading: 50.0
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("50.0"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", baseTime.plus(10, ChronoUnit.MINUTES)
        ));

        // 1 INVALID reading: 99.0
        sensorReadingRepository.save(SensorReading.create(
                testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("99.0"),
                MeasurementUnit.PERCENT, ReadingQuality.INVALID, "TEST", baseTime.plus(20, ChronoUnit.MINUTES)
        ));

        entityManager.flush();
        entityManager.clear();

        List<TelemetryAggregate> aggregates = sensorAnalyticsRepository.aggregateBySensor(
                testSensor.getId(),
                baseTime,
                baseTime.plus(1, ChronoUnit.HOURS),
                AggregationInterval.HOURLY,
                ReadingQuality.VALID
        );

        assertThat(aggregates).hasSize(1);
        assertThat(aggregates.get(0).readingCount()).isEqualTo(1);
        assertThat(aggregates.get(0).average()).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    @Test
    @DisplayName("calculateDataQuality computes health distribution across validity classifications")
    void calculateDataQuality_evaluatesCorrectly() {
        Instant now = Instant.now();
        Instant past = now.minus(1, ChronoUnit.HOURS);

        sensorReadingRepository.save(SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("50.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "T", past.plus(10, ChronoUnit.MINUTES)));
        sensorReadingRepository.save(SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("52.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "T", past.plus(20, ChronoUnit.MINUTES)));
        sensorReadingRepository.save(SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("51.0"), MeasurementUnit.PERCENT, ReadingQuality.SUSPECT, "T", past.plus(30, ChronoUnit.MINUTES)));
        sensorReadingRepository.save(SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("53.0"), MeasurementUnit.PERCENT, ReadingQuality.INVALID, "T", past.plus(40, ChronoUnit.MINUTES)));

        entityManager.flush();
        entityManager.clear();

        DataQualityMetrics metrics = sensorAnalyticsRepository.calculateDataQuality(
                testSensor.getId(),
                fieldId,
                past,
                now
        );

        assertThat(metrics.totalReadings()).isEqualTo(4);
        assertThat(metrics.validReadings()).isEqualTo(2);
        assertThat(metrics.suspectReadings()).isEqualTo(1);
        assertThat(metrics.invalidReadings()).isEqualTo(1);
        assertThat(metrics.validPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("calculateAvailability properly identifies communicating vs unresponsive sensors")
    void calculateAvailability_identifiesUnresponsiveSensors() {
        // Sensor 1: Active and seen 5 minutes ago (communicating)
        Sensor s1 = Sensor.create(fieldId, "S1", "SN-AV-1-" + UUID.randomUUID(), SensorType.TEMPERATURE, "SIMULATED", null);
        s1.recordHeartbeat(Instant.now().minus(5, ChronoUnit.MINUTES));
        sensorRepository.save(s1);

        // Sensor 2: Active but seen 2 hours ago (unresponsive)
        Sensor s2 = Sensor.create(fieldId, "S2", "SN-AV-2-" + UUID.randomUUID(), SensorType.WATER_FLOW, "SIMULATED", null);
        s2.recordHeartbeat(Instant.now().minus(2, ChronoUnit.HOURS));
        sensorRepository.save(s2);

        // Sensor 3: Maintenance
        Sensor s3 = Sensor.create(fieldId, "S3", "SN-AV-3-" + UUID.randomUUID(), SensorType.WATER_LEVEL, "SIMULATED", null);
        s3.setMaintenance();
        sensorRepository.save(s3);

        entityManager.flush();
        entityManager.clear();

        SensorAvailabilitySummary availability = sensorAnalyticsRepository.calculateAvailability(
                fieldId,
                Duration.ofMinutes(30)
        );

        // total sensors on this field: testSensor (active, no heartbeat so unresponsive) + s1 (active, communicating) + s2 (active, unresponsive) + s3 (maintenance)
        assertThat(availability.totalSensors()).isGreaterThanOrEqualTo(4);
        assertThat(availability.communicatingCount()).isGreaterThanOrEqualTo(1);
        assertThat(availability.unresponsiveCount()).isGreaterThanOrEqualTo(2);
        assertThat(availability.maintenanceCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deleteByMeasuredAtBefore purges expired telemetry while preserving recent observations")
    void deleteByMeasuredAtBefore_purgesExpiredReadings() {
        Instant now = Instant.now();
        Instant old = now.minus(400, ChronoUnit.DAYS);

        SensorReading rOld = SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("45.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", old);
        SensorReading rRecent = SensorReading.create(testSensor.getId(), fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("46.0"), MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", now);

        sensorReadingRepository.save(rOld);
        sensorReadingRepository.save(rRecent);

        entityManager.flush();
        entityManager.clear();

        int deleted = sensorReadingRepository.deleteByMeasuredAtBefore(now.minus(365, ChronoUnit.DAYS));
        assertThat(deleted).isGreaterThanOrEqualTo(1);

        // Recent reading is still present
        assertThat(sensorReadingRepository.findLatestBySensorId(testSensor.getId())).isPresent();
    }
}
