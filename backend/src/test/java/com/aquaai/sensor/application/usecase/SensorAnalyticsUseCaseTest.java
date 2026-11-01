package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.DataQualityMetrics;
import com.aquaai.sensor.domain.analytics.FieldAnalyticsSummaryData;
import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorAnalyticsUseCaseTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorReadingRepository sensorReadingRepository;

    @Mock
    private SensorAnalyticsRepository sensorAnalyticsRepository;

    @Mock
    private FieldOwnershipChecker fieldOwnershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private GetSensorAnalyticsUseCase getSensorAnalyticsUseCase;
    private GetFieldAnalyticsUseCase getFieldAnalyticsUseCase;
    private GetFieldAnalyticsSummaryUseCase getFieldAnalyticsSummaryUseCase;

    private final UUID farmerId = UUID.randomUUID();
    private final UUID fieldId = UUID.randomUUID();
    private final UUID sensorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getSensorAnalyticsUseCase = new GetSensorAnalyticsUseCase(
                sensorRepository,
                sensorAnalyticsRepository,
                fieldOwnershipChecker,
                currentUserProvider
        );
        getFieldAnalyticsUseCase = new GetFieldAnalyticsUseCase(
                sensorAnalyticsRepository,
                fieldOwnershipChecker,
                currentUserProvider
        );
        getFieldAnalyticsSummaryUseCase = new GetFieldAnalyticsSummaryUseCase(
                sensorReadingRepository,
                sensorAnalyticsRepository,
                fieldOwnershipChecker,
                currentUserProvider
        );
    }

    @Test
    @DisplayName("GetSensorAnalyticsUseCase succeeds with validated parameters")
    void getSensorAnalytics_success() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        Sensor sensor = Sensor.create(fieldId, "Soil Probe", "SN-01", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));

        Instant from = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant to = Instant.now();

        TelemetryAggregate agg = new TelemetryAggregate(
                fieldId, sensorId, SensorType.SOIL_MOISTURE, AggregationInterval.HOURLY,
                from, from.plus(1, ChronoUnit.HOURS), new BigDecimal("48.0"), new BigDecimal("47.0"),
                new BigDecimal("49.0"), null, new BigDecimal("47.0"), from, new BigDecimal("49.0"),
                from.plus(50, ChronoUnit.MINUTES), 5, MeasurementUnit.PERCENT
        );
        when(sensorAnalyticsRepository.aggregateBySensor(sensorId, from, to, AggregationInterval.HOURLY, ReadingQuality.VALID))
                .thenReturn(List.of(agg));

        List<TelemetryAggregate> result = getSensorAnalyticsUseCase.execute(
                new GetSensorAnalyticsUseCase.Query(sensorId, from, to, AggregationInterval.HOURLY, ReadingQuality.VALID)
        );

        assertThat(result).hasSize(1);
        verify(fieldOwnershipChecker).getFieldForRead(fieldId, farmerId, UserRole.FARMER);
    }

    @Test
    @DisplayName("GetSensorAnalyticsUseCase rejects invalid date range (from > to)")
    void getSensorAnalytics_invalidRange_throwsException() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        Sensor sensor = Sensor.create(fieldId, "Soil Probe", "SN-01", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));

        Instant now = Instant.now();
        Instant future = now.plus(2, ChronoUnit.DAYS);

        assertThatThrownBy(() -> getSensorAnalyticsUseCase.execute(
                new GetSensorAnalyticsUseCase.Query(sensorId, future, now, AggregationInterval.HOURLY, null)
        )).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("GetSensorAnalyticsUseCase rejects query range exceeding interval max limit")
    void getSensorAnalytics_rangeExceeded_throwsException() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        Sensor sensor = Sensor.create(fieldId, "Soil Probe", "SN-01", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));

        // 5m interval max allowed is 7 days; requesting 20 days should be rejected
        Instant now = Instant.now();
        Instant past = now.minus(20, ChronoUnit.DAYS);

        assertThatThrownBy(() -> getSensorAnalyticsUseCase.execute(
                new GetSensorAnalyticsUseCase.Query(sensorId, past, now, AggregationInterval.FIVE_MINUTES, null)
        )).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("GetFieldAnalyticsUseCase enforces parcel ownership and delegates to repository")
    void getFieldAnalytics_success() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        Instant from = Instant.now().minus(3, ChronoUnit.DAYS);
        Instant to = Instant.now();

        TelemetryAggregate agg = new TelemetryAggregate(
                fieldId, null, SensorType.TEMPERATURE, AggregationInterval.HOURLY,
                from, from.plus(1, ChronoUnit.HOURS), new BigDecimal("25.0"), new BigDecimal("22.0"),
                new BigDecimal("28.0"), null, new BigDecimal("22.0"), from, new BigDecimal("27.0"),
                from.plus(45, ChronoUnit.MINUTES), 4, MeasurementUnit.CELSIUS
        );
        when(sensorAnalyticsRepository.aggregateByField(fieldId, SensorType.TEMPERATURE, from, to, AggregationInterval.HOURLY, ReadingQuality.VALID))
                .thenReturn(List.of(agg));

        List<TelemetryAggregate> result = getFieldAnalyticsUseCase.execute(
                new GetFieldAnalyticsUseCase.Query(fieldId, SensorType.TEMPERATURE, from, to, AggregationInterval.HOURLY, ReadingQuality.VALID)
        );

        assertThat(result).hasSize(1);
        verify(fieldOwnershipChecker).getFieldForRead(fieldId, farmerId, UserRole.FARMER);
    }

    @Test
    @DisplayName("GetFieldAnalyticsSummaryUseCase aggregates latest readings, availability, and quality")
    void getFieldAnalyticsSummary_success() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        SensorReading reading = SensorReading.create(
                sensorId, fieldId, SensorType.SOIL_MOISTURE, new BigDecimal("49.5"),
                MeasurementUnit.PERCENT, ReadingQuality.VALID, "TEST", Instant.now()
        );
        when(sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId)).thenReturn(List.of(reading));

        SensorAvailabilitySummary availability = new SensorAvailabilitySummary(2, 2, 0, 0, 0, 2, 0);
        when(sensorAnalyticsRepository.calculateAvailability(eq(fieldId), any(Duration.class))).thenReturn(availability);

        DataQualityMetrics quality = DataQualityMetrics.calculate(50, 48, 1, 1, 0, 0);
        when(sensorAnalyticsRepository.calculateDataQuality(eq(null), eq(fieldId), any(Instant.class), any(Instant.class)))
                .thenReturn(quality);

        FieldAnalyticsSummaryData summary = getFieldAnalyticsSummaryUseCase.execute(fieldId);

        assertThat(summary.fieldId()).isEqualTo(fieldId);
        assertThat(summary.latestReadings()).containsKey(SensorType.SOIL_MOISTURE);
        assertThat(summary.availability().activeCount()).isEqualTo(2);
        assertThat(summary.past24hDataQuality().validPercentage()).isEqualTo(96.0);
    }
}
