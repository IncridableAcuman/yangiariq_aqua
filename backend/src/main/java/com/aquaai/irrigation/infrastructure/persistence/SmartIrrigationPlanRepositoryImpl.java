package com.aquaai.irrigation.infrastructure.persistence;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SmartIrrigationPlanRepositoryImpl implements SmartIrrigationPlanRepository {

    private final SpringDataSmartIrrigationPlanRepository springDataRepository;
    private final SmartIrrigationPlanJpaMapper mapper;

    private static final Set<IrrigationPlanStatus> ACTIVE_STATUSES = Set.of(
            IrrigationPlanStatus.PROPOSED,
            IrrigationPlanStatus.APPROVED,
            IrrigationPlanStatus.SCHEDULED,
            IrrigationPlanStatus.IN_PROGRESS
    );

    @Override
    @Transactional
    public SmartIrrigationPlan save(SmartIrrigationPlan plan) {
        SmartIrrigationPlanJpaEntity entity = mapper.toJpaEntity(plan);
        SmartIrrigationPlanJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SmartIrrigationPlan> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartIrrigationPlan> findByFieldId(UUID fieldId) {
        return springDataRepository.findByFieldIdOrderByPlannedDateDesc(fieldId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartIrrigationPlan> findByFieldIdIn(Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataRepository.findByFieldIdInOrderByPlannedDateDesc(fieldIds)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SmartIrrigationPlan> findActiveByFieldId(UUID fieldId) {
        return springDataRepository.findFirstByFieldIdAndStatusInOrderByPlannedDateDesc(fieldId, ACTIVE_STATUSES)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartIrrigationPlan> findActiveByFieldIdIn(Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataRepository.findByFieldIdInAndStatusInOrderByPlannedDateDesc(fieldIds, ACTIVE_STATUSES)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartIrrigationPlan> findByPlannedDate(LocalDate plannedDate) {
        return springDataRepository.findByPlannedDate(plannedDate)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActivePlanForDate(UUID fieldId, LocalDate plannedDate) {
        Set<IrrigationPlanStatus> terminalStatuses = Set.of(
                IrrigationPlanStatus.CANCELLED,
                IrrigationPlanStatus.EXPIRED,
                IrrigationPlanStatus.COMPLETED
        );
        return springDataRepository.existsByFieldIdAndPlannedDateAndStatusNotIn(
                fieldId,
                plannedDate,
                terminalStatuses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumVolumeByFieldIdAndStatuses(UUID fieldId, Collection<IrrigationPlanStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = springDataRepository.sumVolumeByFieldIdAndStatuses(fieldId, statuses);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmartIrrigationPlan> findFiltered(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        Page<SmartIrrigationPlanJpaEntity> result = springDataRepository.findFiltered(
                fieldId,
                status,
                priority,
                dateFrom,
                dateTo,
                PageRequest.of(page, size)
        );
        return result.stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countFiltered(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return springDataRepository.findFiltered(
                fieldId,
                status,
                priority,
                dateFrom,
                dateTo,
                PageRequest.of(0, 1)
        ).getTotalElements();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }
}
