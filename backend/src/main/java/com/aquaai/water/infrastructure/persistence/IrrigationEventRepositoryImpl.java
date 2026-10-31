package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link IrrigationEventRepository}.
 */
@Repository
@RequiredArgsConstructor
public class IrrigationEventRepositoryImpl implements IrrigationEventRepository {

    private static final java.util.Set<String> ALLOWED_SORT_PROPERTIES = java.util.Set.of(
            "actualVolumeM3",
            "startedAt",
            "endedAt",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final SpringDataIrrigationEventRepository springDataRepository;
    private final IrrigationEventJpaMapper mapper;

    @Override
    @Transactional
    public IrrigationEvent save(IrrigationEvent event) {
        if (event == null) {
            return null;
        }

        IrrigationEventJpaEntity entityToSave;
        if (event.getId() != null) {
            Optional<IrrigationEventJpaEntity> existing = springDataRepository.findById(event.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(event, entityToSave);
            } else {
                entityToSave = mapper.toEntity(event);
            }
        } else {
            entityToSave = mapper.toEntity(event);
        }

        IrrigationEventJpaEntity saved = springDataRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IrrigationEvent> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IrrigationEvent> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds) {
        if (id == null || fieldIds == null || fieldIds.isEmpty()) {
            return Optional.empty();
        }
        return springDataRepository.findByIdAndFieldIdIn(id, fieldIds).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationEvent> findByFieldId(UUID fieldId) {
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
    public List<IrrigationEvent> findByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status) {
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
    public List<IrrigationEvent> findByIrrigationPlanId(UUID irrigationPlanId) {
        if (irrigationPlanId == null) {
            return List.of();
        }
        return springDataRepository.findByIrrigationPlanId(irrigationPlanId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IrrigationEvent> findByFieldIdAndStartedAtBetween(UUID fieldId, Instant start, Instant end) {
        if (fieldId == null || start == null || end == null) {
            return List.of();
        }
        return springDataRepository.findByFieldIdAndStartedAtBetween(fieldId, start, end)
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
    public long countByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status) {
        if (fieldId == null || status == null) {
            return 0;
        }
        return springDataRepository.countByFieldIdAndStatus(fieldId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumActualVolumeByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status) {
        if (fieldId == null || status == null) {
            return BigDecimal.ZERO;
        }
        return springDataRepository.sumActualVolumeByFieldIdAndStatus(fieldId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public com.aquaai.water.domain.model.IrrigationEventPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID irrigationPlanId,
            IrrigationEventStatus status,
            Instant startDate,
            Instant endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? 20 : Math.min(size, 100);

        String safeSortBy = (sortBy != null && ALLOWED_SORT_PROPERTIES.contains(sortBy)) ? sortBy : "startedAt";
        org.springframework.data.domain.Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDirection)
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(safePage, safeSize, org.springframework.data.domain.Sort.by(direction, safeSortBy));

        boolean fieldIdsEmpty = (fieldIds == null || fieldIds.isEmpty());
        java.util.Collection<UUID> safeFieldIds = fieldIdsEmpty ? java.util.Collections.emptySet() : fieldIds;

        org.springframework.data.domain.Page<IrrigationEventJpaEntity> pageResult =
                springDataRepository.findWithFilters(
                        safeFieldIds,
                        fieldIdsEmpty,
                        irrigationPlanId,
                        status,
                        startDate,
                        endDate,
                        pageable
                );

        List<IrrigationEvent> domainItems = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new com.aquaai.water.domain.model.IrrigationEventPage(
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
