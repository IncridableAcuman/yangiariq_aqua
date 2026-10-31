package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIrrigationPlanRepository extends JpaRepository<IrrigationPlanJpaEntity, UUID> {

    List<IrrigationPlanJpaEntity> findByFieldId(UUID fieldId);

    List<IrrigationPlanJpaEntity> findByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status);

    List<IrrigationPlanJpaEntity> findByWaterAllocationId(UUID waterAllocationId);

    List<IrrigationPlanJpaEntity> findByFieldIdAndPlannedStartAtBetween(UUID fieldId, Instant start, Instant end);

    long countByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status);

    Optional<IrrigationPlanJpaEntity> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    @org.springframework.data.jpa.repository.Query("""
        SELECT p FROM IrrigationPlanJpaEntity p
        WHERE (:fieldIdsEmpty = true OR p.fieldId IN :fieldIds)
          AND (:waterAllocationId IS NULL OR p.waterAllocationId = :waterAllocationId)
          AND (:status IS NULL OR p.status = :status)
          AND (cast(:startDate as timestamp) IS NULL OR p.plannedEndAt >= :startDate)
          AND (cast(:endDate as timestamp) IS NULL OR p.plannedStartAt <= :endDate)
    """)
    org.springframework.data.domain.Page<IrrigationPlanJpaEntity> findWithFilters(
            @org.springframework.data.repository.query.Param("fieldIds") java.util.Collection<UUID> fieldIds,
            @org.springframework.data.repository.query.Param("fieldIdsEmpty") boolean fieldIdsEmpty,
            @org.springframework.data.repository.query.Param("waterAllocationId") UUID waterAllocationId,
            @org.springframework.data.repository.query.Param("status") IrrigationPlanStatus status,
            @org.springframework.data.repository.query.Param("startDate") java.time.Instant startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.Instant endDate,
            org.springframework.data.domain.Pageable pageable
    );
}
