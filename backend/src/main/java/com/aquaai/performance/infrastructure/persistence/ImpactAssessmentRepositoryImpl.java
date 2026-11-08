package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import com.aquaai.performance.infrastructure.persistence.mapper.PerformanceJpaMapper;
import com.aquaai.performance.infrastructure.persistence.repository.SpringDataImpactAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImpactAssessmentRepositoryImpl implements ImpactAssessmentRepository {

    private final SpringDataImpactAssessmentRepository repository;

    @Override
    public List<ImpactAssessment> findAll() {
        return repository.findAll().stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ImpactAssessment> findById(UUID id) {
        return repository.findById(id).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public List<ImpactAssessment> findByInterventionType(String interventionType) {
        return repository.findByInterventionType(interventionType).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImpactAssessment> findByAttributionLevel(AttributionLevel attributionLevel) {
        return repository.findByAttributionLevel(attributionLevel).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImpactAssessment> findByReviewStatus(AssessmentReviewStatus status) {
        return repository.findByReviewStatus(status).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImpactAssessment> findByScopeId(UUID scopeId) {
        return repository.findByScopeId(scopeId).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ImpactAssessment save(ImpactAssessment assessment) {
        var entity = PerformanceJpaMapper.toEntity(assessment);
        return PerformanceJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public void saveAll(List<ImpactAssessment> assessments) {
        var entities = assessments.stream()
                .map(PerformanceJpaMapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
