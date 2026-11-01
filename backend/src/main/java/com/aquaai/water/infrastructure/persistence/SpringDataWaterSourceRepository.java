package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterSourceRepository extends JpaRepository<WaterSourceJpaEntity, UUID> {

    Optional<WaterSourceJpaEntity> findByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status);

    List<WaterSourceJpaEntity> findByStatus(WaterSourceStatus status);

    List<WaterSourceJpaEntity> findByType(WaterSourceType type);

    boolean existsByNameIgnoreCaseAndStatus(String name, WaterSourceStatus status);

    long countByStatus(WaterSourceStatus status);

    @org.springframework.data.jpa.repository.Query("""
        SELECT s FROM WaterSourceJpaEntity s
        WHERE (:type IS NULL OR s.type = :type)
          AND (:status IS NULL OR s.status = :status)
          AND (cast(:searchPattern as string) IS NULL OR LOWER(s.name) LIKE cast(:searchPattern as string))
    """)
    org.springframework.data.domain.Page<WaterSourceJpaEntity> findWithFilters(
            @org.springframework.data.repository.query.Param("searchPattern") String searchPattern,
            @org.springframework.data.repository.query.Param("type") WaterSourceType type,
            @org.springframework.data.repository.query.Param("status") WaterSourceStatus status,
            org.springframework.data.domain.Pageable pageable
    );
}
