package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA persistence interface for {@link FieldJpaEntity}.
 */
@Repository
public interface SpringDataFieldRepository extends JpaRepository<FieldJpaEntity, UUID> {

    List<FieldJpaEntity> findByOwnerId(UUID ownerId);

    Optional<FieldJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<FieldJpaEntity> findByOwnerIdAndStatus(UUID ownerId, FieldStatus status);

    boolean existsByOwnerIdAndId(UUID ownerId, UUID id);

    @Query("SELECT (COUNT(f) > 0) FROM FieldJpaEntity f WHERE f.ownerId = :ownerId AND LOWER(f.name) = LOWER(:name) AND f.status = :status")
    boolean existsByOwnerIdAndNameIgnoreCaseAndStatus(
            @Param("ownerId") UUID ownerId,
            @Param("name") String name,
            @Param("status") FieldStatus status
    );

    @Query("SELECT (COUNT(f) > 0) FROM FieldJpaEntity f WHERE f.ownerId = :ownerId AND LOWER(f.name) = LOWER(:name) AND f.status = :status AND f.id != :id")
    boolean existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
            @Param("ownerId") UUID ownerId,
            @Param("name") String name,
            @Param("status") FieldStatus status,
            @Param("id") UUID id
    );

    long countByOwnerId(UUID ownerId);

    long countByStatus(FieldStatus status);

    @Query("""
        SELECT f FROM FieldJpaEntity f
        WHERE (:ownerId IS NULL OR f.ownerId = :ownerId)
          AND (:status IS NULL OR f.status = :status)
          AND (:cropType IS NULL OR f.cropType = :cropType)
          AND (:soilType IS NULL OR f.soilType = :soilType)
          AND (:irrigationMethod IS NULL OR f.irrigationMethod = :irrigationMethod)
    """)
    Page<FieldJpaEntity> findWithFilters(
            @Param("ownerId") UUID ownerId,
            @Param("status") FieldStatus status,
            @Param("cropType") CropType cropType,
            @Param("soilType") SoilType soilType,
            @Param("irrigationMethod") IrrigationMethod irrigationMethod,
            Pageable pageable
    );
}
