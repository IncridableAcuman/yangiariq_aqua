package com.aquaai.recommendation.domain.port;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationSummaryData;
import com.aquaai.recommendation.domain.model.RecommendationType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for AgronomicRecommendation aggregate persistence.
 */
public interface RecommendationRepository {

    AgronomicRecommendation save(AgronomicRecommendation recommendation);

    Optional<AgronomicRecommendation> findById(UUID id);

    Optional<AgronomicRecommendation> findActiveByFieldAndType(UUID fieldId, RecommendationType type);

    List<AgronomicRecommendation> findActiveByFieldId(UUID fieldId);

    List<AgronomicRecommendation> findActiveByFieldIdIn(java.util.Collection<UUID> fieldIds);

    List<AgronomicRecommendation> findByFieldId(UUID fieldId, int page, int size);

    List<AgronomicRecommendation> findAll(
            UUID fieldId,
            RecommendationStatus status,
            RecommendationPriority priority,
            RecommendationType type,
            Instant from,
            Instant to,
            int page,
            int size
    );

    long countAll(
            UUID fieldId,
            RecommendationStatus status,
            RecommendationPriority priority,
            RecommendationType type,
            Instant from,
            Instant to
    );

    RecommendationSummaryData getSummary(UUID fieldId);

    List<AgronomicRecommendation> findExpiredActiveRecommendations(Instant now);
}
