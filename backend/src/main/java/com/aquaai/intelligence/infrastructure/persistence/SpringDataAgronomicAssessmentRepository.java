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

    Page<AgronomicAssessmentJpaEntity> findByFieldIdAndAssessedAtBetween(
            UUID fieldId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    long countByFieldIdAndAssessedAtBetween(UUID fieldId, Instant from, Instant to);
}
