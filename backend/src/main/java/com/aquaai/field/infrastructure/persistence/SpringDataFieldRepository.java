package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.FieldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA persistence interface for {@link FieldJpaEntity}.
 */
@Repository
public interface SpringDataFieldRepository extends JpaRepository<FieldJpaEntity, UUID> {

    List<FieldJpaEntity> findByOwnerId(UUID ownerId);

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
}
