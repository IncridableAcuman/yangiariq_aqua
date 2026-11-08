package com.aquaai.performance.infrastructure.persistence.repository;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.infrastructure.persistence.entity.PerformanceIndicatorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPerformanceIndicatorRepository extends JpaRepository<PerformanceIndicatorJpaEntity, UUID> {
    Optional<PerformanceIndicatorJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
}
