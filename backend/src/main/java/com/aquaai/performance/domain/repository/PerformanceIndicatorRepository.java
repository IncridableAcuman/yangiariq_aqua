package com.aquaai.performance.domain.repository;

import com.aquaai.performance.domain.model.PerformanceIndicator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceIndicatorRepository {
    List<PerformanceIndicator> findAll();
    Optional<PerformanceIndicator> findById(UUID id);
    Optional<PerformanceIndicator> findByCode(String code);
    PerformanceIndicator save(PerformanceIndicator indicator);
    void saveAll(List<PerformanceIndicator> indicators);
    boolean existsByCode(String code);
}
