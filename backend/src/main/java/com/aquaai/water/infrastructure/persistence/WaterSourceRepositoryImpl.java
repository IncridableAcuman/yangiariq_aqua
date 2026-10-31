package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import com.aquaai.water.domain.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link WaterSourceRepository}.
 */
@Repository
@RequiredArgsConstructor
public class WaterSourceRepositoryImpl implements WaterSourceRepository {

    private static final java.util.Set<String> ALLOWED_SORT_PROPERTIES = java.util.Set.of(
            "name",
            "type",
            "status",
            "createdAt",
            "updatedAt"
    );

    private final SpringDataWaterSourceRepository springDataRepository;
    private final WaterSourceJpaMapper mapper;

    @Override
    @Transactional
    public WaterSource save(WaterSource waterSource) {
        if (waterSource == null) {
            return null;
        }

        WaterSourceJpaEntity entityToSave;
        if (waterSource.getId() != null) {
            Optional<WaterSourceJpaEntity> existing = springDataRepository.findById(waterSource.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(waterSource, entityToSave);
            } else {
                entityToSave = mapper.toEntity(waterSource);
            }
        } else {
            entityToSave = mapper.toEntity(waterSource);
        }

        WaterSourceJpaEntity saved = springDataRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaterSource> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WaterSource> findByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status) {
        if (name == null || status == null) {
            return Optional.empty();
        }
        return springDataRepository.findByNameIgnoreCaseAndStatus(name.trim(), status).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterSource> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterSource> findByStatus(WaterSourceStatus status) {
        if (status == null) {
            return List.of();
        }
        return springDataRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterSource> findByType(WaterSourceType type) {
        if (type == null) {
            return List.of();
        }
        return springDataRepository.findByType(type)
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
    public boolean existsByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status) {
        if (name == null || status == null) {
            return false;
        }
        return springDataRepository.existsByNameIgnoreCaseAndStatus(name.trim(), status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(WaterSourceStatus status) {
        if (status == null) {
            return 0;
        }
        return springDataRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public com.aquaai.water.domain.model.WaterSourcePage findWithFilters(
            String search,
            WaterSourceType type,
            WaterSourceStatus status,
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

        String safeSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        org.springframework.data.domain.Page<WaterSourceJpaEntity> pageResult =
                springDataRepository.findWithFilters(safeSearch, type, status, pageable);

        List<WaterSource> domainItems = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new com.aquaai.water.domain.model.WaterSourcePage(
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
