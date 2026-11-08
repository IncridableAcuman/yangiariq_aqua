package com.aquaai.performance.domain.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceEvaluation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceEvaluationRepository {
    List<PerformanceEvaluation> findAll();
    Optional<PerformanceEvaluation> findById(UUID id);
    List<PerformanceEvaluation> findByIndicatorCode(String indicatorCode);
    List<PerformanceEvaluation> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);
    List<PerformanceEvaluation> findByPeriodBetween(Instant from, Instant to);
    Optional<PerformanceEvaluation> findFirstByIndicatorCodeOrderByEvaluatedAtDesc(String indicatorCode);
    PerformanceEvaluation save(PerformanceEvaluation evaluation);
    void saveAll(List<PerformanceEvaluation> evaluations);
}
