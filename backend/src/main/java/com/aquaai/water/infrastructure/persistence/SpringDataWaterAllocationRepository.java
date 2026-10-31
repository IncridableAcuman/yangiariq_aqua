package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterAllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterAllocationRepository extends JpaRepository<WaterAllocationJpaEntity, UUID> {

    List<WaterAllocationJpaEntity> findByFieldId(UUID fieldId);

    List<WaterAllocationJpaEntity> findByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status);

    List<WaterAllocationJpaEntity> findByWaterSourceId(UUID waterSourceId);

    List<WaterAllocationJpaEntity> findByWaterSourceIdAndStatus(UUID waterSourceId, WaterAllocationStatus status);

    long countByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status);

    @Query("SELECT COALESCE(SUM(a.allocatedVolumeM3), 0) FROM WaterAllocationJpaEntity a WHERE a.fieldId = :fieldId AND a.status = :status")
    BigDecimal sumAllocatedVolumeByFieldIdAndStatus(@Param("fieldId") UUID fieldId, @Param("status") WaterAllocationStatus status);

    Optional<WaterAllocationJpaEntity> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    @Query("""
        SELECT a FROM WaterAllocationJpaEntity a
        WHERE (:fieldIdsEmpty = true OR a.fieldId IN :fieldIds)
          AND (:waterSourceId IS NULL OR a.waterSourceId = :waterSourceId)
          AND (:status IS NULL OR a.status = :status)
          AND (cast(:periodStart as timestamp) IS NULL OR a.periodEnd >= :periodStart)
          AND (cast(:periodEnd as timestamp) IS NULL OR a.periodStart <= :periodEnd)
    """)
    org.springframework.data.domain.Page<WaterAllocationJpaEntity> findWithFilters(
            @Param("fieldIds") java.util.Collection<UUID> fieldIds,
            @Param("fieldIdsEmpty") boolean fieldIdsEmpty,
            @Param("waterSourceId") UUID waterSourceId,
            @Param("status") WaterAllocationStatus status,
            @Param("periodStart") java.time.Instant periodStart,
            @Param("periodEnd") java.time.Instant periodEnd,
            org.springframework.data.domain.Pageable pageable
    );
}
