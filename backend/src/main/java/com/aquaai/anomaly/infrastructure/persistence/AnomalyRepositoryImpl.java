package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.sensor.domain.model.SensorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AnomalyRepositoryImpl implements AnomalyRepository {

    private final SpringDataAnomalyRepository springDataRepository;
    private final AnomalyJpaMapper mapper;

    @Override
    public Anomaly save(Anomaly anomaly) {
        AnomalyJpaEntity entity = mapper.toEntity(anomaly);
        AnomalyJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Anomaly> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Anomaly> findActiveByFieldAndSensorAndType(
            UUID fieldId,
            UUID sensorId,
            AnomalyType anomalyType
    ) {
        return springDataRepository.findActiveAnomaly(fieldId, sensorId, anomalyType)
                .map(mapper::toDomain);
    }

    @Override
    public List<Anomaly> findAll(
            UUID fieldId,
            UUID sensorId,
            AnomalyStatus status,
            AnomalySeverity severity,
            AnomalyType anomalyType,
            SensorType sensorType,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "detectedAt"));
        Page<AnomalyJpaEntity> resultPage = springDataRepository.findWithFilters(
                fieldId, sensorId, status, severity, anomalyType, sensorType, from, to, pageRequest
        );
        return resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(
            UUID fieldId,
            UUID sensorId,
            AnomalyStatus status,
            AnomalySeverity severity,
            AnomalyType anomalyType,
            SensorType sensorType,
            Instant from,
            Instant to
    ) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        return springDataRepository.findWithFilters(
                fieldId, sensorId, status, severity, anomalyType, sensorType, from, to, pageRequest
        ).getTotalElements();
    }

    @Override
    public long countActiveByField(UUID fieldId) {
        return springDataRepository.countActiveByFieldId(fieldId);
    }

    @Override
    public List<Anomaly> findActiveByFieldIdIn(java.util.Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return springDataRepository.findActiveByFieldIdIn(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByStatus(AnomalyStatus status) {
        return springDataRepository.countByStatus(status);
    }

    @Override
    public long countBySeverity(AnomalySeverity severity) {
        return springDataRepository.countBySeverity(severity);
    }
}
