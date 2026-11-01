package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link WaterAllocationRepository}.
 */
@Repository
@RequiredArgsConstructor
public class WaterAllocationRepositoryImpl implements WaterAllocationRepository {

    private static final java.util.Set<String> ALLOWED_SORT_PROPERTIES = java.util.Set.of(
            "allocatedVolumeM3",
            "periodStart",
            "periodEnd",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final SpringDataWaterAllocationRepository springDataRepository;
    private final WaterAllocationJpaMapper mapper;

    @Override
    @Transactional
    public WaterAllocation save(WaterAllocation allocation) {
        if (allocation == null) {
            return null;
        }

        WaterAllocationJpaEntity entityToSave;
        if (allocation.getId() != null) {
            Optional<WaterAllocationJpaEntity> existing = springDataRepository.findById(allocation.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(allocation, entityToSave);
            } else {
                entityToSave = mapper.toEntity(allocation);
            }
        } else {
            entityToSave = mapper.toEntity(allocation);
        }

        WaterAllocationJpaEntity saved = springDataRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaterAllocation> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaterAllocation> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds) {
        if (id == null || fieldIds == null || fieldIds.isEmpty()) {
            return Optional.empty();
        }
        return springDataRepository.findByIdAndFieldIdIn(id, fieldIds).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterAllocation> findByFieldId(UUID fieldId) {
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
    public List<WaterAllocation> findByFieldIdIn(java.util.Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return List.of();
        }
        return springDataRepository.findByFieldIdIn(fieldIds)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterAllocation> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterAllocation> findByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status) {
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
    public List<WaterAllocation> findByWaterSourceId(UUID waterSourceId) {
        if (waterSourceId == null) {
            return List.of();
        }
        return springDataRepository.findByWaterSourceId(waterSourceId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterAllocation> findByWaterSourceIdAndStatus(UUID waterSourceId, WaterAllocationStatus status) {
        if (waterSourceId == null || status == null) {
            return List.of();
        }
        return springDataRepository.findByWaterSourceIdAndStatus(waterSourceId, status)
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
    public long countByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status) {
        if (fieldId == null || status == null) {
            return 0;
        }
        return springDataRepository.countByFieldIdAndStatus(fieldId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAllocatedVolumeByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status) {
        if (fieldId == null || status == null) {
            return BigDecimal.ZERO;
        }
        return springDataRepository.sumAllocatedVolumeByFieldIdAndStatus(fieldId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public com.aquaai.water.domain.model.WaterAllocationPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID waterSourceId,
            WaterAllocationStatus status,
            java.time.Instant periodStart,
            java.time.Instant periodEnd,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? 20 : Math.min(size, 100);

        String safeSortBy = (sortBy != null && ALLOWED_SORT_PROPERTIES.contains(sortBy)) ? sortBy : "createdAt";
        org.springframework.data.domain.Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDirection)
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(safePage, safeSize, org.springframework.data.domain.Sort.by(direction, safeSortBy));

        boolean fieldIdsEmpty = (fieldIds == null || fieldIds.isEmpty());
        java.util.Collection<UUID> safeFieldIds = fieldIdsEmpty ? java.util.Collections.emptySet() : fieldIds;

        org.springframework.data.domain.Page<WaterAllocationJpaEntity> pageResult =
                springDataRepository.findWithFilters(
                        safeFieldIds,
                        fieldIdsEmpty,
                        waterSourceId,
                        status,
                        periodStart,
                        periodEnd,
                        pageable
                );

        List<WaterAllocation> domainItems = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new com.aquaai.water.domain.model.WaterAllocationPage(
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
