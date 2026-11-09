package com.aquaai.integration.application.service;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.integration.application.dto.IngestBatchCommand;
import com.aquaai.integration.application.dto.IngestTelemetryItemCommand;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.integration.domain.model.EvidenceClassification;
import com.aquaai.integration.domain.model.IngestedProvenance;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.domain.repository.IngestedProvenanceRepository;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionPipelineUnitTest {

    @Mock
    private DataSourceRepository dataSourceRepository;
    @Mock
    private IngestedProvenanceRepository provenanceRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private SensorReadingRepository sensorReadingRepository;
    @Mock
    private FieldRepository fieldRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IngestionPipelineService pipelineService;

    private DataSource activeSource;
    private Field testField;
    private Sensor testSensor;

    @BeforeEach
    void setUp() {
        pipelineService = new IngestionPipelineService(
                dataSourceRepository,
                provenanceRepository,
                sensorRepository,
                sensorReadingRepository,
                fieldRepository,
                eventPublisher
        );

        activeSource = DataSource.create(
                "TEST_GATEWAY",
                "Test Gateway",
                DataSourceType.SENSOR_DEVICE,
                "TestAdapter",
                "Testing",
                null,
                null,
                DataSourceStatus.ACTIVE
        );

        UUID fieldId = UUID.randomUUID();
        testField = Field.create(UUID.randomUUID(), "Parcel 101", "Yangiariq field",
                BigDecimal.valueOf(5.0), CropType.COTTON, SoilType.LOAM, IrrigationMethod.DRIP, null, null);

        testSensor = Sensor.create(testField.getId(), "Moisture Sensor", "DEV-101-MOIST",
                SensorType.SOIL_MOISTURE, "TEST_GATEWAY", "Sensor");

        lenient().when(dataSourceRepository.findBySourceCode("TEST_GATEWAY")).thenReturn(Optional.of(activeSource));
        lenient().when(fieldRepository.findById(testField.getId())).thenReturn(Optional.of(testField));
        lenient().when(sensorRepository.findByFieldId(testField.getId())).thenReturn(List.of(testSensor));
        lenient().when(sensorReadingRepository.save(any(SensorReading.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully ingest valid telemetry and persist provenance")
    void shouldIngestValidTelemetry() {
        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                "EXT-REC-001",
                testField.getId(),
                testSensor.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(48.5),
                "%",
                Instant.now().minus(10, ChronoUnit.MINUTES),
                "OBSERVED"
        );

        IngestBatchCommand batch = new IngestBatchCommand("TEST_GATEWAY", List.of(item));

        when(provenanceRepository.existsByIdempotencyKey(any())).thenReturn(false);

        IngestionResultDto result = pipelineService.processBatch(batch);

        assertThat(result.totalReceived()).isEqualTo(1);
        assertThat(result.validCount()).isEqualTo(1);
        assertThat(result.rejectedCount()).isEqualTo(0);

        verify(sensorReadingRepository, times(1)).save(any(SensorReading.class));

        ArgumentCaptor<IngestedProvenance> provCaptor = ArgumentCaptor.forClass(IngestedProvenance.class);
        verify(provenanceRepository, times(1)).save(provCaptor.capture());
        assertThat(provCaptor.getValue().getQualityStatus()).isEqualTo(DataQualityStatus.VALID);
        assertThat(provCaptor.getValue().getEvidenceClassification()).isEqualTo(EvidenceClassification.OBSERVED);
        assertThat(provCaptor.getValue().getCanonicalUnit()).isEqualTo(MeasurementUnit.PERCENT);
    }

    @Test
    @DisplayName("Should reject duplicate measurements based on idempotency key")
    void shouldRejectDuplicateMeasurements() {
        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                "EXT-REC-DUPLICATE",
                testField.getId(),
                testSensor.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(50.0),
                "%",
                Instant.now(),
                "OBSERVED"
        );

        when(provenanceRepository.existsByIdempotencyKey("TEST_GATEWAY:EXT-REC-DUPLICATE")).thenReturn(true);

        IngestionResultDto result = pipelineService.processBatch(new IngestBatchCommand("TEST_GATEWAY", List.of(item)));

        assertThat(result.validCount()).isEqualTo(0);
        assertThat(result.duplicateCount()).isEqualTo(1);
        assertThat(result.rejectedCount()).isEqualTo(1);

        verify(sensorReadingRepository, never()).save(any());
        verify(provenanceRepository, times(1)).save(any(IngestedProvenance.class));
    }

    @Test
    @DisplayName("Should reject measurements exceeding physical bounds as OUT_OF_RANGE")
    void shouldRejectOutOfBounds() {
        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                "EXT-REC-BOUNDS",
                testField.getId(),
                testSensor.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(150.0), // Max for SOIL_MOISTURE is 100.0%
                "%",
                Instant.now(),
                "OBSERVED"
        );

        when(provenanceRepository.existsByIdempotencyKey(any())).thenReturn(false);

        IngestionResultDto result = pipelineService.processBatch(new IngestBatchCommand("TEST_GATEWAY", List.of(item)));

        assertThat(result.validCount()).isEqualTo(0);
        assertThat(result.rejectedCount()).isEqualTo(1);

        ArgumentCaptor<IngestedProvenance> provCaptor = ArgumentCaptor.forClass(IngestedProvenance.class);
        verify(provenanceRepository, times(1)).save(provCaptor.capture());
        assertThat(provCaptor.getValue().getQualityStatus()).isEqualTo(DataQualityStatus.OUT_OF_RANGE);
    }

    @Test
    @DisplayName("Should reject future timestamps beyond 5 minute clock skew")
    void shouldRejectFutureTimestamp() {
        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                "EXT-REC-FUTURE",
                testField.getId(),
                testSensor.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(45.0),
                "%",
                Instant.now().plus(20, ChronoUnit.MINUTES),
                "OBSERVED"
        );

        IngestionResultDto result = pipelineService.processBatch(new IngestBatchCommand("TEST_GATEWAY", List.of(item)));

        assertThat(result.validCount()).isEqualTo(0);
        assertThat(result.rejectedCount()).isEqualTo(1);

        ArgumentCaptor<IngestedProvenance> provCaptor = ArgumentCaptor.forClass(IngestedProvenance.class);
        verify(provenanceRepository, times(1)).save(provCaptor.capture());
        assertThat(provCaptor.getValue().getQualityStatus()).isEqualTo(DataQualityStatus.INVALID);
    }

    @Test
    @DisplayName("Should enforce SIMULATED classification for simulated data sources")
    void shouldEnforceSimulatedClassification() {
        DataSource simSource = DataSource.create(
                "SIMULATED_TEST", "Sim Source", DataSourceType.SIMULATED_PROVIDER,
                "SimAdapter", null, null, null, DataSourceStatus.ACTIVE
        );
        when(dataSourceRepository.findBySourceCode("SIMULATED_TEST")).thenReturn(Optional.of(simSource));

        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                "REC-SIM",
                testField.getId(),
                testSensor.getId(),
                null,
                SensorType.SOIL_MOISTURE,
                BigDecimal.valueOf(45.0),
                "%",
                Instant.now(),
                "VERIFIED" // Attempt to claim VERIFIED for simulated source!
        );

        when(provenanceRepository.existsByIdempotencyKey(any())).thenReturn(false);

        pipelineService.processBatch(new IngestBatchCommand("SIMULATED_TEST", List.of(item)));

        ArgumentCaptor<IngestedProvenance> provCaptor = ArgumentCaptor.forClass(IngestedProvenance.class);
        verify(provenanceRepository, times(1)).save(provCaptor.capture());
        // Must strictly remain SIMULATED!
        assertThat(provCaptor.getValue().getEvidenceClassification()).isEqualTo(EvidenceClassification.SIMULATED);
    }
}
