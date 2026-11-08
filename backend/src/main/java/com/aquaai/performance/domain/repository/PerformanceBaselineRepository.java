package com.aquaai.performance.domain.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceBaseline;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceBaselineRepository {
    List<PerformanceBaseline> findAll();
    Optional<PerformanceBaseline> findById(UUID id);
    List<PerformanceBaseline> findByIndicatorCode(String indicatorCode);
    List<PerformanceBaseline> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);
    Optional<PerformanceBaseline> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType);
    PerformanceBaseline save(PerformanceBaseline baseline);
    void saveAll(List<PerformanceBaseline> baselines);
}
