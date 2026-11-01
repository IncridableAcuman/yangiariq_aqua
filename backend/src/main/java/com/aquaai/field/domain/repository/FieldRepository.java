package com.aquaai.field.domain.repository;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldPage;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;

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

    List<Field> findAll();

    Optional<Field> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Field> findByOwnerId(UUID ownerId);

    List<Field> findByOwnerIdAndStatus(UUID ownerId, FieldStatus status);

    boolean existsById(UUID id);

    boolean existsByOwnerIdAndId(UUID ownerId, UUID id);

    boolean existsByOwnerIdAndNameIgnoreCaseAndStatus(UUID ownerId, String name, FieldStatus status);

    boolean existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(UUID ownerId, String name, FieldStatus status, UUID id);

    long countByOwnerId(UUID ownerId);

    long countByStatus(FieldStatus status);

    FieldPage findWithFilters(
            UUID ownerId,
            FieldStatus status,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    void deleteById(UUID id);
}
