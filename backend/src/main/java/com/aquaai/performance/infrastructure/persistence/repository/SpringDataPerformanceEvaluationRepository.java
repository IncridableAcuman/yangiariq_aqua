package com.aquaai.performance.infrastructure.persistence.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceEvaluationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluationJpaEntity, UUID> {
    List<PerformanceEvaluationJpaEntity> findByIndicatorCode(String indicatorCode);
    List<PerformanceEvaluationJpaEntity> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);

    @Query("SELECT e FROM PerformanceEvaluationJpaEntity e WHERE e.periodStart >= :from AND e.periodEnd <= :to")
    List<PerformanceEvaluationJpaEntity> findByPeriodBetween(@Param("from") Instant from, @Param("to") Instant to);

    Optional<PerformanceEvaluationJpaEntity> findFirstByIndicatorCodeOrderByEvaluatedAtDesc(String indicatorCode);
}
