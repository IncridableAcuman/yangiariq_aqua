package com.aquaai.performance.domain.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceTarget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceTargetRepository {
    List<PerformanceTarget> findAll();
    Optional<PerformanceTarget> findById(UUID id);
    List<PerformanceTarget> findByIndicatorCode(String indicatorCode);
    List<PerformanceTarget> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);
    Optional<PerformanceTarget> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType);
    PerformanceTarget save(PerformanceTarget target);
    void saveAll(List<PerformanceTarget> targets);
}
