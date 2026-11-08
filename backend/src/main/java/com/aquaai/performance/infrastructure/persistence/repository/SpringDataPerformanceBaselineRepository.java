package com.aquaai.performance.infrastructure.persistence.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceBaselineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPerformanceBaselineRepository extends JpaRepository<PerformanceBaselineJpaEntity, UUID> {
    List<PerformanceBaselineJpaEntity> findByIndicatorCode(String indicatorCode);
    List<PerformanceBaselineJpaEntity> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);
    Optional<PerformanceBaselineJpaEntity> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType);
}
