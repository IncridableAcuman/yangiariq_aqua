package com.aquaai.integration.infrastructure.persistence.repository;

import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.infrastructure.persistence.entity.IngestedProvenanceJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIngestedProvenanceRepository extends JpaRepository<IngestedProvenanceJpaEntity, UUID> {

    Optional<IngestedProvenanceJpaEntity> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<IngestedProvenanceJpaEntity> findByDataSourceIdOrderByIngestedAtDesc(UUID dataSourceId, Pageable pageable);

    List<IngestedProvenanceJpaEntity> findByFieldIdOrderByMeasuredAtDesc(UUID fieldId, Pageable pageable);

    List<IngestedProvenanceJpaEntity> findAllByOrderByIngestedAtDesc(Pageable pageable);

    long countByQualityStatus(DataQualityStatus qualityStatus);

    @Query("SELECT p.qualityStatus, COUNT(p) FROM IngestedProvenanceJpaEntity p WHERE p.ingestedAt >= :since GROUP BY p.qualityStatus")
    List<Object[]> countByQualityStatusSince(@Param("since") Instant since);
}
