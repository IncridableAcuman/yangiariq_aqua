package com.aquaai.performance.infrastructure.persistence.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceTargetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPerformanceTargetRepository extends JpaRepository<PerformanceTargetJpaEntity, UUID> {
    List<PerformanceTargetJpaEntity> findByIndicatorCode(String indicatorCode);
    List<PerformanceTargetJpaEntity> findByIndicatorCodeAndScopeType(String indicatorCode, GeographicScopeType scopeType);
    Optional<PerformanceTargetJpaEntity> findFirstByIndicatorCodeAndScopeTypeOrderByCreatedAtDesc(String indicatorCode, GeographicScopeType scopeType);
}
