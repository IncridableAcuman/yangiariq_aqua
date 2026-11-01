package com.aquaai.intelligence.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAgronomicAssessmentRepository extends JpaRepository<AgronomicAssessmentJpaEntity, UUID> {

    Optional<AgronomicAssessmentJpaEntity> findFirstByFieldIdOrderByAssessedAtDesc(UUID fieldId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AgronomicAssessmentJpaEntity a WHERE a.fieldId IN :fieldIds " +
            "AND a.assessedAt = (SELECT MAX(a2.assessedAt) FROM AgronomicAssessmentJpaEntity a2 WHERE a2.fieldId = a.fieldId)")
    java.util.List<AgronomicAssessmentJpaEntity> findLatestByFieldIdIn(@org.springframework.data.repository.query.Param("fieldIds") java.util.Collection<UUID> fieldIds);

    Page<AgronomicAssessmentJpaEntity> findByFieldIdAndAssessedAtBetween(
            UUID fieldId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    long countByFieldIdAndAssessedAtBetween(UUID fieldId, Instant from, Instant to);
}
