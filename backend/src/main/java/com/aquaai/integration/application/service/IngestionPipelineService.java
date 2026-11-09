package com.aquaai.integration.application.service;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.integration.application.dto.IngestBatchCommand;
import com.aquaai.integration.application.dto.IngestTelemetryItemCommand;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.EvidenceClassification;
import com.aquaai.integration.domain.model.IngestedProvenance;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.domain.repository.IngestedProvenanceRepository;
import com.aquaai.integration.domain.service.CanonicalUnitConverter;
import com.aquaai.sensor.application.event.TelemetryReadingRecordedEvent;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.sensor.domain.repository.SensorRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionPipelineService {

    private final DataSourceRepository dataSourceRepository;
    private final IngestedProvenanceRepository provenanceRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final FieldRepository fieldRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final CanonicalUnitConverter unitConverter = new CanonicalUnitConverter();

    @Transactional
    public IngestionResultDto processBatch(IngestBatchCommand command) {
        if (command == null || command.items() == null || command.items().isEmpty()) {
            return IngestionResultDto.of(0, 0, 0, 0, List.of("Empty ingestion payload"));
        }

        String sourceCode = (command.sourceCode() != null && !command.sourceCode().isBlank())
                ? command.sourceCode().trim().toUpperCase()
                : "HTTP_SENSOR_GATEWAY";

        DataSource dataSource = dataSourceRepository.findBySourceCode(sourceCode)
                .orElseGet(() -> {
                    DataSource defaultSource = DataSource.create(
                            sourceCode,
                            "External Gateway (" + sourceCode + ")",
                            com.aquaai.integration.domain.model.DataSourceType.SENSOR_DEVICE,
                            "ExternalGatewayAdapter",
                            "Auto-registered external ingestion gateway",
                            null,
                            "AUTO_REGISTERED",
                            DataSourceStatus.ACTIVE
                    );
                    return dataSourceRepository.save(defaultSource);
                });

        if (!dataSource.getStatus().isOperational()) {
            throw new BusinessRuleException(
                    "Data source '" + sourceCode + "' is not operational (status: " + dataSource.getStatus() + ")",
                    "DATA_SOURCE_INACTIVE"
            );
        }

        int validCount = 0;
        int rejectedCount = 0;
        int duplicateCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < command.items().size(); i++) {
            IngestTelemetryItemCommand item = command.items().get(i);
            try {
                ItemIngestionResult result = ingestSingleItem(dataSource, item);
                if (result.qualityStatus == DataQualityStatus.VALID) {
                    validCount++;
                } else if (result.qualityStatus == DataQualityStatus.DUPLICATE) {
                    duplicateCount++;
                    rejectedCount++;
                    errors.add("Row " + (i + 1) + ": Duplicate measurement detected");
                } else {
                    rejectedCount++;
                    errors.add("Row " + (i + 1) + ": " + result.validationMessage);
                }
            } catch (Exception e) {
                rejectedCount++;
                String msg = "Row " + (i + 1) + ": " + e.getMessage();
                errors.add(msg);
                log.warn("Ingestion failure: {}", msg);
            }
        }

        dataSourceRepository.save(dataSource);

        return IngestionResultDto.of(command.items().size(), validCount, rejectedCount, duplicateCount, errors);
    }

    private record ItemIngestionResult(
            DataQualityStatus qualityStatus,
            String validationMessage,
            UUID sensorReadingId
    ) {}

    private ItemIngestionResult ingestSingleItem(DataSource dataSource, IngestTelemetryItemCommand item) {
        // Step 1: Raw validation
        if (item.value() == null) {
            recordRejection(dataSource, item, DataQualityStatus.INCOMPLETE, "Measurement value is null");
            return new ItemIngestionResult(DataQualityStatus.INCOMPLETE, "Measurement value is null", null);
        }
        if (item.sensorType() == null) {
            recordRejection(dataSource, item, DataQualityStatus.INVALID, "Sensor type is null");
            return new ItemIngestionResult(DataQualityStatus.INVALID, "Sensor type is null", null);
        }
        if (item.fieldId() == null) {
            recordRejection(dataSource, item, DataQualityStatus.INCOMPLETE, "Field ID is required");
            return new ItemIngestionResult(DataQualityStatus.INCOMPLETE, "Field ID is required", null);
        }

        // Validate field existence
        Optional<Field> fieldOpt = fieldRepository.findById(item.fieldId());
        if (fieldOpt.isEmpty()) {
            recordRejection(dataSource, item, DataQualityStatus.INVALID, "Field not found: " + item.fieldId());
            return new ItemIngestionResult(DataQualityStatus.INVALID, "Field not found: " + item.fieldId(), null);
        }

        // Step 2: Timestamp validation
        Instant measuredAt = item.measuredAt() != null ? item.measuredAt() : Instant.now();
        Instant maxFutureAllowed = Instant.now().plus(5, ChronoUnit.MINUTES);
        if (measuredAt.isAfter(maxFutureAllowed)) {
            recordRejection(dataSource, item, DataQualityStatus.INVALID, "Measurement timestamp is in the future: " + measuredAt);
            return new ItemIngestionResult(DataQualityStatus.INVALID, "Future timestamp", null);
        }

        boolean isStale = measuredAt.isBefore(Instant.now().minus(30, ChronoUnit.DAYS));

        // Step 3: Normalization & Canonical Unit Conversion
        String rawUnit = (item.unit() != null && !item.unit().isBlank())
                ? item.unit().trim()
                : item.sensorType().getDefaultUnit().getSymbol();

        CanonicalUnitConverter.ConversionResult conversion;
        try {
            conversion = unitConverter.convertToCanonical(item.sensorType(), item.value(), rawUnit);
        } catch (Exception e) {
            recordRejection(dataSource, item, DataQualityStatus.INVALID, "Unit conversion failed: " + e.getMessage());
            return new ItemIngestionResult(DataQualityStatus.INVALID, e.getMessage(), null);
        }

        BigDecimal normalizedVal = conversion.normalizedValue();
        MeasurementUnit canonicalUnit = conversion.canonicalUnit();

        // Step 4: Duplicate Detection & Idempotency Key
        String idempotencyKey = buildIdempotencyKey(dataSource.getSourceCode(), item, measuredAt);
        if (provenanceRepository.existsByIdempotencyKey(idempotencyKey)) {
            recordRejection(dataSource, item, DataQualityStatus.DUPLICATE, "Duplicate telemetry record key: " + idempotencyKey);
            return new ItemIngestionResult(DataQualityStatus.DUPLICATE, "Duplicate record", null);
        }

        // Step 5: Physical Bounds Assessment
        BigDecimal min = item.sensorType().getMinPhysicalValue();
        BigDecimal max = item.sensorType().getMaxPhysicalValue();
        if ((min != null && normalizedVal.compareTo(min) < 0) || (max != null && normalizedVal.compareTo(max) > 0)) {
            String msg = String.format("Value %s %s out of physical bounds [%s, %s]", normalizedVal, canonicalUnit, min, max);
            recordRejection(dataSource, item, DataQualityStatus.OUT_OF_RANGE, msg);
            return new ItemIngestionResult(DataQualityStatus.OUT_OF_RANGE, msg, null);
        }

        // Step 6: Evidence Classification
        EvidenceClassification evidence = resolveEvidenceClassification(dataSource, item.evidenceClassification());

        // Step 7: Resolve or provision Sensor Device
        Sensor sensor = resolveSensorDevice(item.fieldId(), item.sensorId(), item.deviceIdentifier(), item.sensorType(), dataSource.getSourceCode());

        // Step 8: Domain Persistence & Event Dispatch
        ReadingQuality domainQuality = isStale ? ReadingQuality.SUSPECT : ReadingQuality.VALID;
        SensorReading reading = SensorReading.create(
                sensor.getId(),
                item.fieldId(),
                item.sensorType(),
                normalizedVal,
                canonicalUnit,
                domainQuality,
                dataSource.getSourceCode(),
                measuredAt
        );

        SensorReading savedReading = sensorReadingRepository.save(reading);

        // Update sensor heartbeat
        sensor.recordHeartbeat(measuredAt);
        sensorRepository.save(sensor);

        // Step 9: Save Provenance
        DataQualityStatus finalQuality = isStale ? DataQualityStatus.STALE : DataQualityStatus.VALID;
        IngestedProvenance provenance = IngestedProvenance.create(
                dataSource.getId(),
                item.externalRecordId(),
                idempotencyKey,
                savedReading.getId(),
                item.fieldId(),
                sensor.getId(),
                item.sensorType(),
                item.value(),
                rawUnit,
                normalizedVal,
                canonicalUnit,
                finalQuality,
                evidence,
                isStale ? "Historical measurement (stale)" : "Successfully validated",
                measuredAt
        );
        provenanceRepository.save(provenance);

        dataSource.incrementIngested(1);

        // Publish event for downstream anomaly & analytics engine
        eventPublisher.publishEvent(new TelemetryReadingRecordedEvent(savedReading));

        return new ItemIngestionResult(finalQuality, "Valid", savedReading.getId());
    }

    private String buildIdempotencyKey(String sourceCode, IngestTelemetryItemCommand item, Instant measuredAt) {
        if (item.externalRecordId() != null && !item.externalRecordId().isBlank()) {
            return sourceCode + ":" + item.externalRecordId().trim();
        }
        return sourceCode + ":" + item.fieldId() + ":" + item.sensorType() + ":" + measuredAt.toEpochMilli();
    }

    private EvidenceClassification resolveEvidenceClassification(DataSource dataSource, String requestedEvidence) {
        if (dataSource.getSourceType() == com.aquaai.integration.domain.model.DataSourceType.SIMULATED_PROVIDER) {
            return EvidenceClassification.SIMULATED;
        }
        if (requestedEvidence != null && !requestedEvidence.isBlank()) {
            try {
                EvidenceClassification parsed = EvidenceClassification.valueOf(requestedEvidence.trim().toUpperCase());
                // Never allow external inputs to self-certify as VERIFIED without operator confirmation
                if (parsed == EvidenceClassification.VERIFIED && dataSource.getSourceType() != com.aquaai.integration.domain.model.DataSourceType.OPERATOR_INPUT) {
                    return EvidenceClassification.OBSERVED;
                }
                return parsed;
            } catch (IllegalArgumentException ignored) {}
        }
        return EvidenceClassification.OBSERVED;
    }

    private Sensor resolveSensorDevice(UUID fieldId, UUID sensorId, String deviceIdentifier, SensorType sensorType, String provider) {
        if (sensorId != null) {
            Optional<Sensor> sensorOpt = sensorRepository.findById(sensorId);
            if (sensorOpt.isPresent() && sensorOpt.get().getFieldId().equals(fieldId)) {
                return sensorOpt.get();
            }
        }

        // Try lookup by device identifier if provided
        if (deviceIdentifier != null && !deviceIdentifier.isBlank()) {
            Optional<Sensor> sensorOpt = sensorRepository.findByDeviceIdentifier(deviceIdentifier.trim());
            if (sensorOpt.isPresent()) {
                return sensorOpt.get();
            }
        }

        // Lookup any active sensor on the field of matching type
        List<Sensor> fieldSensors = sensorRepository.findByFieldId(fieldId);
        for (Sensor s : fieldSensors) {
            if (s.getType() == sensorType && s.isActive()) {
                return s;
            }
        }

        // Auto-provision an integration sensor device for this parcel
        String identifier = "DEV-" + fieldId.toString().substring(0, 8).toUpperCase() + "-" + sensorType.name();
        Sensor newSensor = Sensor.create(
                fieldId,
                sensorType.name() + " (" + provider + ")",
                identifier,
                sensorType,
                provider,
                "Auto-provisioned sensor device for external telemetry data ingestion"
        );
        return sensorRepository.save(newSensor);
    }

    private void recordRejection(DataSource dataSource, IngestTelemetryItemCommand item, DataQualityStatus status, String reason) {
        Instant measuredAt = item.measuredAt() != null ? item.measuredAt() : Instant.now();
        String idempotencyKey = buildIdempotencyKey(dataSource.getSourceCode(), item, measuredAt);
        IngestedProvenance provenance = IngestedProvenance.create(
                dataSource.getId(),
                item.externalRecordId(),
                idempotencyKey,
                null,
                item.fieldId() != null ? item.fieldId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                item.sensorId(),
                item.sensorType() != null ? item.sensorType() : SensorType.SOIL_MOISTURE,
                item.value() != null ? item.value() : BigDecimal.ZERO,
                item.unit() != null ? item.unit() : "UNKNOWN",
                BigDecimal.ZERO,
                item.sensorType() != null ? item.sensorType().getDefaultUnit() : MeasurementUnit.PERCENT,
                status,
                EvidenceClassification.MISSING,
                reason,
                measuredAt
        );
        provenanceRepository.save(provenance);
        dataSource.incrementRejected(1);
    }
}
