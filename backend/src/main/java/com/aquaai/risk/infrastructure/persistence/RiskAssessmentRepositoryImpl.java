package com.aquaai.risk.infrastructure.persistence;

import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RiskAssessmentRepositoryImpl implements RiskAssessmentRepository {

    private final SpringDataRiskAssessmentRepository springDataRepository;
    private final RiskAssessmentJpaMapper mapper;

    @Override
    public FarmRiskAssessment save(FarmRiskAssessment assessment) {
        if (assessment == null) return null;
        RiskAssessmentJpaEntity entity = mapper.toJpaEntity(assessment);
        RiskAssessmentJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FarmRiskAssessment> findById(UUID id) {
        if (id == null) return Optional.empty();
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<FarmRiskAssessment> findLatestByFieldId(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        return springDataRepository.findLatestByFieldId(fieldId).map(mapper::toDomain);
    }

    @Override
    public List<FarmRiskAssessment> findAllLatestByFieldIds(Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) return Collections.emptyList();
        return springDataRepository.findAllLatestByFieldIds(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<FarmRiskAssessment> findByFieldIdHistory(UUID fieldId, Instant from, Instant to, int page, int size) {
        if (fieldId == null) return Collections.emptyList();
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return springDataRepository.findHistorical(fieldId, from, to, pageRequest).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countHistoricalByFieldId(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null) return 0;
        return springDataRepository.countHistorical(fieldId, from, to);
    }

    @Override
    public long countBySeverity(RiskSeverity severity, Collection<UUID> fieldIds) {
        if (severity == null || fieldIds == null || fieldIds.isEmpty()) return 0;
        return springDataRepository.countBySeverityAndFieldIds(severity.name(), fieldIds);
    }

    @Override
    public long countByLifecycleStatus(RiskAssessmentLifecycleStatus status, Collection<UUID> fieldIds) {
        if (status == null || fieldIds == null || fieldIds.isEmpty()) return 0;
        return springDataRepository.countByLifecycleStatusAndFieldIds(status.name(), fieldIds);
    }
}
