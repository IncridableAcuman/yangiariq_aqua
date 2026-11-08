package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import com.aquaai.performance.infrastructure.persistence.mapper.PerformanceJpaMapper;
import com.aquaai.performance.infrastructure.persistence.repository.SpringDataPerformanceTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerformanceTargetRepositoryImpl implements PerformanceTargetRepository {

    private final SpringDataPerformanceTargetRepository repository;

    @Override
    public List<PerformanceTarget> findAll() {
        return repository.findAll().stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceTarget> findById(UUID id) {
        return repository.findById(id).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public List<PerformanceTarget> findByIndicatorCode(String indicatorCode) {
        return repository.findByIndicatorCode(indicatorCode).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PerformanceTarget> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType) {
        return repository.findByIndicatorCodeAndScopeType(indicatorCode, scopeType).stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceTarget> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType) {
        return repository.findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(indicatorCode, scopeType)
                .map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public PerformanceTarget save(PerformanceTarget target) {
        var entity = PerformanceJpaMapper.toEntity(target);
        return PerformanceJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public void saveAll(List<PerformanceTarget> targets) {
        var entities = targets.stream()
                .map(PerformanceJpaMapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
