package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.infrastructure.persistence.mapper.PerformanceJpaMapper;
import com.aquaai.performance.infrastructure.persistence.repository.SpringDataPerformanceIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerformanceIndicatorRepositoryImpl implements PerformanceIndicatorRepository {

    private final SpringDataPerformanceIndicatorRepository repository;

    @Override
    public List<PerformanceIndicator> findAll() {
        return repository.findAll().stream()
                .map(PerformanceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PerformanceIndicator> findById(UUID id) {
        return repository.findById(id).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public Optional<PerformanceIndicator> findByCode(String code) {
        return repository.findByCode(code).map(PerformanceJpaMapper::toDomain);
    }

    @Override
    public PerformanceIndicator save(PerformanceIndicator indicator) {
        var entity = PerformanceJpaMapper.toEntity(indicator);
        return PerformanceJpaMapper.toDomain(repository.save(entity));
    }

    @Override
    public void saveAll(List<PerformanceIndicator> indicators) {
        var entities = indicators.stream()
                .map(PerformanceJpaMapper::toEntity)
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }
}
