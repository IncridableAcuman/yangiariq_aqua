package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of domain {@link FieldRepository}.
 * Encapsulates Spring Data JPA and entity mapping behind the domain repository port.
 */
@Repository
@RequiredArgsConstructor
public class FieldRepositoryImpl implements FieldRepository {

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
    public void deleteById(UUID id) {
        if (id != null) {
            springDataFieldRepository.deleteById(id);
        }
    }
}
