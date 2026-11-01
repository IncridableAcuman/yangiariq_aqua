package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.sensor.application.service.SensorSimulationService;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.provider.SensorProvider;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
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
import java.time.Instant;
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
class SensorUseCaseUnitTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorReadingRepository sensorReadingRepository;

    @Mock
    private FieldOwnershipChecker fieldOwnershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private SensorProvider sensorProvider;

    @Mock
    private Field mockField;

    private final UUID farmerId = UUID.randomUUID();
    private final UUID fieldId = UUID.randomUUID();
    private final UUID sensorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
    }

    @Test
    @DisplayName("CreateSensorUseCase successfully creates sensor after ownership verification")
    void createSensor_success() {
        CreateSensorUseCase useCase = new CreateSensorUseCase(sensorRepository, fieldOwnershipChecker, currentUserProvider);

        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);
        when(sensorRepository.existsByDeviceIdentifier("SN-001")).thenReturn(false);
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSensorUseCase.Command command = new CreateSensorUseCase.Command(
                fieldId,
                "Moisture Probe",
                "SN-001",
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                "Test probe"
        );

        Sensor created = useCase.execute(command);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Moisture Probe");
        assertThat(created.getDeviceIdentifier()).isEqualTo("SN-001");
        verify(sensorRepository).save(any(Sensor.class));
    }

    @Test
    @DisplayName("CreateSensorUseCase rejects duplicate deviceIdentifier")
    void createSensor_duplicateIdentifier_throwsException() {
        CreateSensorUseCase useCase = new CreateSensorUseCase(sensorRepository, fieldOwnershipChecker, currentUserProvider);

        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);
        when(sensorRepository.existsByDeviceIdentifier("SN-DUPLICATE")).thenReturn(true);

        CreateSensorUseCase.Command command = new CreateSensorUseCase.Command(
                fieldId,
                "Moisture Probe",
                "SN-DUPLICATE",
                SensorType.SOIL_MOISTURE,
                "SIMULATED",
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Sensor with deviceIdentifier 'SN-DUPLICATE' already exists");
    }

    @Test
    @DisplayName("GetSensorUseCase retrieves sensor and enforces anti-IDOR field ownership")
    void getSensor_success() {
        GetSensorUseCase useCase = new GetSensorUseCase(sensorRepository, fieldOwnershipChecker, currentUserProvider);
        Sensor sensor = Sensor.create(sensorId, fieldId, "Probe", "SN-101", SensorType.SOIL_MOISTURE, "SIMULATED", null);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForRead(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);

        Sensor result = useCase.execute(sensorId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sensorId);
    }

    @Test
    @DisplayName("GetSensorUseCase throws 404 Not Found when field ownership check fails (Anti-IDOR)")
    void getSensor_unownedField_throws404() {
        GetSensorUseCase useCase = new GetSensorUseCase(sensorRepository, fieldOwnershipChecker, currentUserProvider);
        Sensor sensor = Sensor.create(sensorId, fieldId, "Probe", "SN-101", SensorType.SOIL_MOISTURE, "SIMULATED", null);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForRead(fieldId, farmerId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", fieldId));

        assertThatThrownBy(() -> useCase.execute(sensorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sensor");
    }

    @Test
    @DisplayName("RecordSensorReadingUseCase records telemetry and updates sensor lastSeenAt")
    void recordReading_success() {
        RecordSensorReadingUseCase useCase = new RecordSensorReadingUseCase(
                sensorRepository,
                sensorReadingRepository,
                fieldOwnershipChecker,
                currentUserProvider,
                eventPublisher
        );
        Sensor sensor = Sensor.create(sensorId, fieldId, "Probe", "SN-101", SensorType.SOIL_MOISTURE, "SIMULATED", null);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);
        when(sensorReadingRepository.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordSensorReadingUseCase.Command command = new RecordSensorReadingUseCase.Command(
                sensorId,
                new BigDecimal("62.30"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "MANUAL_TEST",
                Instant.now()
        );

        SensorReading recorded = useCase.execute(command);

        assertThat(recorded).isNotNull();
        assertThat(recorded.getValue()).isEqualByComparingTo(new BigDecimal("62.3000"));
        assertThat(sensor.getLastSeenAt()).isNotNull();
        verify(sensorRepository).save(sensor);
    }

    @Test
    @DisplayName("RecordSensorReadingUseCase rejects recording if sensor is inactive")
    void recordReading_inactiveSensor_throwsException() {
        RecordSensorReadingUseCase useCase = new RecordSensorReadingUseCase(
                sensorRepository,
                sensorReadingRepository,
                fieldOwnershipChecker,
                currentUserProvider,
                eventPublisher
        );
        Sensor sensor = Sensor.create(sensorId, fieldId, "Probe", "SN-101", SensorType.SOIL_MOISTURE, "SIMULATED", null);
        sensor.deactivate();

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);

        RecordSensorReadingUseCase.Command command = new RecordSensorReadingUseCase.Command(
                sensorId,
                new BigDecimal("50.00"),
                MeasurementUnit.PERCENT,
                ReadingQuality.VALID,
                "MANUAL_TEST",
                Instant.now()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot record telemetry observation for sensor with status INACTIVE");
    }

    @Test
    @DisplayName("ListSensorReadingsUseCase validates date range from <= to")
    void listReadings_invalidDateRange_throwsException() {
        ListSensorReadingsUseCase useCase = new ListSensorReadingsUseCase(
                sensorRepository,
                sensorReadingRepository,
                fieldOwnershipChecker,
                currentUserProvider
        );
        Sensor sensor = Sensor.create(sensorId, fieldId, "Probe", "SN-101", SensorType.SOIL_MOISTURE, "SIMULATED", null);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForRead(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);

        Instant from = Instant.now();
        Instant to = from.minusSeconds(3600); // from > to

        ListSensorReadingsUseCase.Query query = new ListSensorReadingsUseCase.Query(
                sensorId,
                from,
                to,
                0,
                20,
                "desc"
        );

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    @DisplayName("SensorSimulationService generates and persists simulated reading")
    void simulationService_simulateSensor_success() {
        SensorSimulationService simulationService = new SensorSimulationService(
                sensorRepository,
                sensorReadingRepository,
                sensorProvider,
                fieldOwnershipChecker,
                currentUserProvider,
                eventPublisher
        );
        Sensor sensor = Sensor.create(sensorId, fieldId, "Flow Meter", "SN-FL-01", SensorType.WATER_FLOW, "SIMULATED", null);

        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(mockField);

        SensorReading mockReading = SensorReading.create(
                sensorId,
                fieldId,
                SensorType.WATER_FLOW,
                new BigDecimal("15.20"),
                MeasurementUnit.CUBIC_METER_PER_HOUR,
                ReadingQuality.VALID,
                "SIMULATED_SENSOR_PROVIDER",
                Instant.now()
        );
        when(sensorProvider.read(sensor)).thenReturn(mockReading);
        when(sensorReadingRepository.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorReading result = simulationService.simulateReadingForSensor(sensorId);

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("15.2000"));
        assertThat(sensor.getLastSeenAt()).isNotNull();
        verify(sensorRepository).save(sensor);
    }
}
