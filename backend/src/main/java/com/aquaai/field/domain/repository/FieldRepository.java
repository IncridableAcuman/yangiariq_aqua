package com.aquaai.field.domain.repository;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for Field persistence.
 * Separated from persistence frameworks (Spring Data / JPA) following Clean Architecture.
 */
public interface FieldRepository {

    Field save(Field field);

    Optional<Field> findById(UUID id);

    List<Field> findByOwnerId(UUID ownerId);

    List<Field> findByOwnerIdAndStatus(UUID ownerId, FieldStatus status);

    boolean existsById(UUID id);

    boolean existsByOwnerIdAndId(UUID ownerId, UUID id);

    boolean existsByOwnerIdAndNameIgnoreCaseAndStatus(UUID ownerId, String name, FieldStatus status);

    boolean existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(UUID ownerId, String name, FieldStatus status, UUID id);

    long countByOwnerId(UUID ownerId);

    long countByStatus(FieldStatus status);

    void deleteById(UUID id);
}
