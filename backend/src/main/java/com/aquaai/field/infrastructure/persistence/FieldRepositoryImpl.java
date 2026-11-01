package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldPage;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link FieldRepository}.
 * Encapsulates Spring Data JPA and entity mapping behind the domain repository port.
 */
@Repository
@RequiredArgsConstructor
public class FieldRepositoryImpl implements FieldRepository {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt",
            "updatedAt",
            "name",
            "areaHectares",
            "status"
    );

    private final SpringDataFieldRepository springDataFieldRepository;
    private final FieldJpaMapper mapper;

    @Override
    public Field save(Field field) {
        if (field == null) {
            return null;
        }

        FieldJpaEntity entityToSave;
        if (field.getId() != null) {
            Optional<FieldJpaEntity> existing = springDataFieldRepository.findById(field.getId());
            if (existing.isPresent()) {
                entityToSave = existing.get();
                mapper.updateEntityFromDomain(field, entityToSave);
            } else {
                entityToSave = mapper.toEntity(field);
            }
        } else {
            entityToSave = mapper.toEntity(field);
        }

        FieldJpaEntity saved = springDataFieldRepository.saveAndFlush(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Field> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataFieldRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Field> findAll() {
        return springDataFieldRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Field> findByIdAndOwnerId(UUID id, UUID ownerId) {
        if (id == null || ownerId == null) {
            return Optional.empty();
        }
        return springDataFieldRepository.findByIdAndOwnerId(id, ownerId).map(mapper::toDomain);
    }

    @Override
    public List<Field> findByOwnerId(UUID ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        return springDataFieldRepository.findByOwnerId(ownerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Field> findByOwnerIdAndStatus(UUID ownerId, FieldStatus status) {
        if (ownerId == null || status == null) {
            return List.of();
        }
        return springDataFieldRepository.findByOwnerIdAndStatus(ownerId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return springDataFieldRepository.existsById(id);
    }

    @Override
    public boolean existsByOwnerIdAndId(UUID ownerId, UUID id) {
        if (ownerId == null || id == null) {
            return false;
        }
        return springDataFieldRepository.existsByOwnerIdAndId(ownerId, id);
    }

    @Override
    public boolean existsByOwnerIdAndNameIgnoreCaseAndStatus(UUID ownerId, String name, FieldStatus status) {
        if (ownerId == null || name == null || status == null) {
            return false;
        }
        return springDataFieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(ownerId, name.trim(), status);
    }

    @Override
    public boolean existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(UUID ownerId, String name, FieldStatus status, UUID id) {
        if (ownerId == null || name == null || status == null || id == null) {
            return false;
        }
        return springDataFieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(ownerId, name.trim(), status, id);
    }

    @Override
    public long countByOwnerId(UUID ownerId) {
        if (ownerId == null) {
            return 0;
        }
        return springDataFieldRepository.countByOwnerId(ownerId);
    }

    @Override
    public long countByStatus(FieldStatus status) {
        if (status == null) {
            return 0;
        }
        return springDataFieldRepository.countByStatus(status);
    }

    @Override
    public FieldPage findWithFilters(
            UUID ownerId,
            FieldStatus status,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? 20 : Math.min(size, 100);

        String safeSortBy = (sortBy != null && ALLOWED_SORT_PROPERTIES.contains(sortBy)) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        Page<FieldJpaEntity> pageResult = springDataFieldRepository.findWithFilters(
                ownerId,
                status,
                cropType,
                soilType,
                irrigationMethod,
                pageable
        );

        List<Field> domainFields = pageResult.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new FieldPage(
                domainFields,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Override
    public void deleteById(UUID id) {
        if (id != null) {
            springDataFieldRepository.deleteById(id);
        }
    }
}
