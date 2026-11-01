package com.aquaai.irrigation.infrastructure.persistence;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSmartIrrigationPlanRepository extends JpaRepository<SmartIrrigationPlanJpaEntity, UUID> {

    List<SmartIrrigationPlanJpaEntity> findByFieldIdOrderByPlannedDateDesc(UUID fieldId);

    Optional<SmartIrrigationPlanJpaEntity> findFirstByFieldIdAndStatusInOrderByPlannedDateDesc(
            UUID fieldId,
            Collection<IrrigationPlanStatus> statuses
    );

    List<SmartIrrigationPlanJpaEntity> findByPlannedDate(LocalDate plannedDate);

    boolean existsByFieldIdAndPlannedDateAndStatusNotIn(
            UUID fieldId,
            LocalDate plannedDate,
            Collection<IrrigationPlanStatus> excludedStatuses
    );

    @Query("SELECT COALESCE(SUM(p.recommendedVolumeM3), 0) FROM SmartIrrigationPlanJpaEntity p " +
           "WHERE p.fieldId = :fieldId AND p.status IN :statuses")
    BigDecimal sumVolumeByFieldIdAndStatuses(
            @Param("fieldId") UUID fieldId,
            @Param("statuses") Collection<IrrigationPlanStatus> statuses
    );

    @Query("SELECT p FROM SmartIrrigationPlanJpaEntity p WHERE " +
           "(:fieldId IS NULL OR p.fieldId = :fieldId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:priority IS NULL OR p.priority = :priority) AND " +
           "(:dateFrom IS NULL OR p.plannedDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR p.plannedDate <= :dateTo) " +
           "ORDER BY p.plannedDate DESC, p.plannedStartAt ASC")
    Page<SmartIrrigationPlanJpaEntity> findFiltered(
            @Param("fieldId") UUID fieldId,
            @Param("status") IrrigationPlanStatus status,
            @Param("priority") IrrigationPriority priority,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );
}
