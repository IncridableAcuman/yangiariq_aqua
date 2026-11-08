package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import com.aquaai.performance.infrastructure.persistence.mapper.PerformanceJpaMapper;
import com.aquaai.performance.infrastructure.persistence.repository.SpringDataPerformanceEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerformanceEvaluationRepositoryImpl implements PerformanceEvaluationRepository {

    private final SpringDataPerformanceEvaluationRepository repository;

    @Override
    public List<PerformanceEvaluation> findAll() {
        return repository.findAll().stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceEvaluation> findById(UUID id) {
        return repository.findById(id).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public List<PerformanceEvaluation> findByIndicatorCode(String indicatorCode) {
        return repository.findByIndicatorCode(indicatorCode).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceEvaluation> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType) {
        return repository.findByIndicatorCodeAndScopeType(indicatorCode, scopeType).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceEvaluation> findByPeriodBetween(Instant from, Instant to) {
        return repository.findByPeriodBetween(from, to).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceEvaluation> findFirstByIndicatorCodeOrderByEvaluatedAtDesc(String indicatorCode) {
        return repository.findFirstByIndicatorCodeOrderByEvaluatedAtDesc(indicatorCode)
                .map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public PerformanceEvaluation save(PerformanceEvaluation evaluation) {
        var entity = PerformanceJpaMapper.toEntity(evaluation);
        return PerformanceJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public void saveAll(List<PerformanceEvaluation> evaluations) {
        var entities = evaluations.stream()
                .map(PerformanceJpaMapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
