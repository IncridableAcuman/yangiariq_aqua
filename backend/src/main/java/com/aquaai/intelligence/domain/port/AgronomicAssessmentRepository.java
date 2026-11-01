package com.aquaai.intelligence.domain.port;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for persisting and querying AgronomicAssessment aggregates.
 */
public interface AgronomicAssessmentRepository {

    AgronomicAssessment save(AgronomicAssessment assessment);

    Optional<AgronomicAssessment> findLatestByFieldId(UUID fieldId);

    List<AgronomicAssessment> findByFieldId(UUID fieldId, Instant from, Instant to, int page, int size);

    long countByFieldId(UUID fieldId, Instant from, Instant to);
}
