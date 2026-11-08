package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import com.aquaai.performance.infrastructure.persistence.mapper.PerformanceJpaMapper;
import com.aquaai.performance.infrastructure.persistence.repository.SpringDataPerformanceBaselineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerformanceBaselineRepositoryImpl implements PerformanceBaselineRepository {

    private final SpringDataPerformanceBaselineRepository repository;

    @Override
    public List<PerformanceBaseline> findAll() {
        return repository.findAll().stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceBaseline> findById(UUID id) {
        return repository.findById(id).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public List<PerformanceBaseline> findByIndicatorCode(String indicatorCode) {
        return repository.findByIndicatorCode(indicatorCode).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceBaseline> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType) {
        return repository.findByIndicatorCodeAndScopeType(indicatorCode, scopeType).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceBaseline> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType) {
        return repository.findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(indicatorCode, scopeType)
                .map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public PerformanceBaseline save(PerformanceBaseline baseline) {
        var entity = PerformanceJpaMapper.toEntity(baseline);
        return PerformanceJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public void saveAll(List<PerformanceBaseline> baselines) {
        var entities = baselines.stream()
                .map(PerformanceJpaMapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
