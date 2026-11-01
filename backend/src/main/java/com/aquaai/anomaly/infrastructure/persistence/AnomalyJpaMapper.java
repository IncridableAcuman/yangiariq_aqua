package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyEvidence;
import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnomalyJpaMapper {

    public Anomaly toDomain(AnomalyJpaEntity entity) {
        if (entity == null) return null;

        List<AnomalyEvidence> evidenceList = new ArrayList<>();
        if (entity.getEvidence() != null) {
            for (AnomalyEvidenceJpaEntity e : entity.getEvidence()) {
                evidenceList.add(new AnomalyEvidence(
                        e.getId(),
                        e.getEvidenceType(),
                        e.getMetric(),
                        e.getObservedValue(),
                        e.getExpectedValue(),
                        e.getMeasuredAt(),
                        e.getMetadata()
                ));
            }
        }

        return new Anomaly(
                entity.getId(),
                entity.getFieldId(),
                entity.getSensorId(),
                entity.getSensorType(),
                entity.getAnomalyType(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getDetectedValue(),
                entity.getExpectedValue(),
                entity.getDeviation(),
                entity.getConfidence(),
                entity.isSimulated(),
                entity.getDescription(),
                entity.getExplanation(),
                entity.getSource(),
                entity.getObservationCount(),
                entity.getFirstObservedAt(),
                entity.getLastObservedAt(),
                entity.getDetectedAt(),
                entity.getAcknowledgedAt(),
                entity.getAcknowledgedBy(),
                entity.getResolvedAt(),
                entity.getResolvedBy(),
                entity.getResolutionNotes(),
                entity.getLastNotifiedAt(),
                evidenceList,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public AnomalyJpaEntity toEntity(Anomaly domain) {
        if (domain == null) return null;

        AnomalyJpaEntity entity = new AnomalyJpaEntity();
        entity.setId(domain.getId());
        entity.setFieldId(domain.getFieldId());
        entity.setSensorId(domain.getSensorId());
        entity.setSensorType(domain.getSensorType());
        entity.setAnomalyType(domain.getAnomalyType());
        entity.setSeverity(domain.getSeverity());
        entity.setStatus(domain.getStatus());
        entity.setDetectedValue(domain.getDetectedValue());
        entity.setExpectedValue(domain.getExpectedValue());
        entity.setDeviation(domain.getDeviation());
        entity.setConfidence(domain.getConfidence());
        entity.setSimulated(domain.isSimulated());
        entity.setDescription(domain.getDescription());
        entity.setExplanation(domain.getExplanation());
        entity.setSource(domain.getSource());
        entity.setObservationCount(domain.getObservationCount());
        entity.setFirstObservedAt(domain.getFirstObservedAt());
        entity.setLastObservedAt(domain.getLastObservedAt());
        entity.setDetectedAt(domain.getDetectedAt());
        entity.setAcknowledgedAt(domain.getAcknowledgedAt());
        entity.setAcknowledgedBy(domain.getAcknowledgedBy());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setResolvedBy(domain.getResolvedBy());
        entity.setResolutionNotes(domain.getResolutionNotes());
        entity.setLastNotifiedAt(domain.getLastNotifiedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        List<AnomalyEvidenceJpaEntity> evidenceEntities = new ArrayList<>();
        if (domain.getEvidence() != null) {
            for (AnomalyEvidence e : domain.getEvidence()) {
                evidenceEntities.add(new AnomalyEvidenceJpaEntity(
                        e.getId(),
                        domain.getId(),
                        e.getEvidenceType(),
                        e.getMetric(),
                        e.getObservedValue(),
                        e.getExpectedValue(),
                        e.getMeasuredAt(),
                        e.getMetadata(),
                        e.getMeasuredAt()
                ));
            }
        }
        entity.setEvidence(evidenceEntities);

        return entity;
    }

    public AnomalyRuleConfig toDomain(AnomalyRuleConfigJpaEntity entity) {
        if (entity == null) return null;
        return new AnomalyRuleConfig(
                entity.getId(),
                entity.getRuleCode(),
                entity.getSensorType(),
                entity.getName(),
                entity.getDescription(),
                entity.isEnabled(),
                entity.getSeverity(),
                entity.getThreshold(),
                entity.getSecondaryThreshold(),
                entity.getTimeWindowMinutes(),
                entity.getMinConsecutiveReadings(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AnomalyRuleConfigJpaEntity toEntity(AnomalyRuleConfig domain) {
        if (domain == null) return null;
        AnomalyRuleConfigJpaEntity entity = new AnomalyRuleConfigJpaEntity();
        entity.setId(domain.getId());
        entity.setRuleCode(domain.getRuleCode());
        entity.setSensorType(domain.getSensorType());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setEnabled(domain.isEnabled());
        entity.setSeverity(domain.getSeverity());
        entity.setThreshold(domain.getThreshold());
        entity.setSecondaryThreshold(domain.getSecondaryThreshold());
        entity.setTimeWindowMinutes(domain.getTimeWindowMinutes());
        entity.setMinConsecutiveReadings(domain.getMinConsecutiveReadings());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
