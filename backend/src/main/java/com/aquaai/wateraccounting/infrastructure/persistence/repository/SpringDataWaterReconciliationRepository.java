package com.aquaai.wateraccounting.infrastructure.persistence.repository;

import com.aquaai.wateraccounting.infrastructure.persistence.entity.WaterReconciliationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWaterReconciliationRepository extends JpaRepository<WaterReconciliationJpaEntity, UUID> {
    List<WaterReconciliationJpaEntity> findByBoundaryTypeOrderByPeriodStartDesc(String boundaryType);
    List<WaterReconciliationJpaEntity> findAllByOrderByCreatedAtDesc();
}
