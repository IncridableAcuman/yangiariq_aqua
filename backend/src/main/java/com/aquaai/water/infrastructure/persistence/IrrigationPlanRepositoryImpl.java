package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link IrrigationPlanRepository}.
 */
@Repository
@RequiredArgsConstructor
public class IrrigationPlanRepositoryImpl implements IrrigationPlanRepository {

    private static final java.util.Set<String> ALLOWED_SORT_PROPERTIES = java.util.Set.of(
            "plannedVolumeM3",
            "plannedStartAt",
            "plannedEndAt",
            "status",
            "method",
            "createdAt",
            "updatedAt"
    );

    private final SpringDataIrrigationPlanRepository springDataRepository;
    private final IrrigationPlanJpaMapper mapper;

    @Override
    @Transactional
    public IrrigationPlan save(IrrigationPlan plan) {
        if (plan == null) {
            return null;
        }

        IrrigationPlanJpaEntity entityToSave;
        if (plan.getId() != null) {
            Optional<IrrigationPlanJpaEntity> existing = springDataRepository.findById(plan.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(plan, entityToSave);
            } else {
                entityToSave = mapper.toEntity(plan);
            }
        } else {
            entityToSave = mapper.toEntity(plan);
        }

        IrrigationPlanJpaEntity saved = springDataRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IrrigationPlan> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IrrigationPlan> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds) {
        if (id == null || fieldIds == null || fieldIds.isEmpty()) {
            return Optional.empty();
        }
        return springDataRepository.findByIdAndFieldIdIn(id, fieldIds).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationPlan> findByFieldId(UUID fieldId) {
        if (fieldId == null) {
            return List.of();
        }
        return springDataRepository.findByFieldId(fieldId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationPlan> findByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status) {
        if (fieldId == null || status == null) {
            return List.of();
        }
        return springDataRepository.findByFieldIdAndStatus(fieldId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationPlan> findByWaterAllocationId(UUID waterAllocationId) {
        if (waterAllocationId == null) {
            return List.of();
        }
        return springDataRepository.findByWaterAllocationId(waterAllocationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationPlan> findByFieldIdAndPlannedStartAtBetween(UUID fieldId, Instant start, Instant end) {
        if (fieldId == null || start == null || end == null) {
            return List.of();
        }
        return springDataRepository.findByFieldIdAndPlannedStartAtBetween(fieldId, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return springDataRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status) {
        if (fieldId == null || status == null) {
            return 0;
        }
        return springDataRepository.countByFieldIdAndStatus(fieldId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public com.aquaai.water.domain.model.IrrigationPlanPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID waterAllocationId,
            IrrigationPlanStatus status,
            Instant startDate,
            Instant endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? 20 : Math.min(size, 100);

        String safeSortBy = (sortBy != null && ALLOWED_SORT_PROPERTIES.contains(sortBy)) ? sortBy : "plannedStartAt";
        org.springframework.data.domain.Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDirection)
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(safePage, safeSize, org.springframework.data.domain.Sort.by(direction, safeSortBy));

        boolean fieldIdsEmpty = (fieldIds == null || fieldIds.isEmpty());
        java.util.Collection<UUID> safeFieldIds = fieldIdsEmpty ? java.util.Collections.emptySet() : fieldIds;

        org.springframework.data.domain.Page<IrrigationPlanJpaEntity> pageResult =
                springDataRepository.findWithFilters(
                        safeFieldIds,
                        fieldIdsEmpty,
                        waterAllocationId,
                        status,
                        startDate,
                        endDate,
                        pageable
                );

        List<IrrigationPlan> domainItems = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new com.aquaai.water.domain.model.IrrigationPlanPage(
                domainItems,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (id != null) {
            springDataRepository.deleteById(id);
        }
    }
}
