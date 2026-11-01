package com.aquaai.recommendation.infrastructure.persistence;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationSummaryData;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecommendationRepositoryImpl implements RecommendationRepository {

    private final SpringDataRecommendationRepository springDataRepository;
    private final RecommendationJpaMapper mapper;

    @Override
    public AgronomicRecommendation save(AgronomicRecommendation recommendation) {
        RecommendationJpaEntity entity = mapper.toEntity(recommendation);
        RecommendationJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AgronomicRecommendation> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AgronomicRecommendation> findActiveByFieldAndType(UUID fieldId, RecommendationType type) {
        return springDataRepository.findActiveRecommendation(fieldId, type)
                .map(mapper::toDomain);
    }

    @Override
    public List<AgronomicRecommendation> findActiveByFieldId(UUID fieldId) {
        return springDataRepository.findActiveByFieldId(fieldId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AgronomicRecommendation> findActiveByFieldIdIn(java.util.Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return springDataRepository.findActiveByFieldIdIn(fieldIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AgronomicRecommendation> findByFieldId(UUID fieldId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "generatedAt"));
        return springDataRepository.findByFieldId(fieldId, pageRequest).getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AgronomicRecommendation> findAll(
            UUID fieldId,
            RecommendationStatus status,
            RecommendationPriority priority,
            RecommendationType type,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "generatedAt"));
        Page<RecommendationJpaEntity> resultPage = springDataRepository.findWithFilters(
                fieldId, status, priority, type, from, to, pageRequest
        );
        return resultPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(
            UUID fieldId,
            RecommendationStatus status,
            RecommendationPriority priority,
            RecommendationType type,
            Instant from,
            Instant to
    ) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        return springDataRepository.findWithFilters(
                fieldId, status, priority, type, from, to, pageRequest
        ).getTotalElements();
    }

    @Override
    public RecommendationSummaryData getSummary(UUID fieldId) {
        long totalActive = springDataRepository.countActive(fieldId);
        long criticalCount = springDataRepository.countByPriority(fieldId, RecommendationPriority.CRITICAL);
        long highCount = springDataRepository.countByPriority(fieldId, RecommendationPriority.HIGH);
        long mediumCount = springDataRepository.countByPriority(fieldId, RecommendationPriority.MEDIUM);
        long lowCount = springDataRepository.countByPriority(fieldId, RecommendationPriority.LOW);
        long infoCount = springDataRepository.countByPriority(fieldId, RecommendationPriority.INFO);

        long generatedCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.GENERATED);
        long viewedCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.VIEWED);
        long acceptedCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.ACCEPTED);
        long dismissedCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.DISMISSED);
        long completedCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.COMPLETED);
        long expiredCount = springDataRepository.countByStatus(fieldId, RecommendationStatus.EXPIRED);

        return new RecommendationSummaryData(
                totalActive,
                criticalCount,
                highCount,
                mediumCount,
                lowCount,
                infoCount,
                generatedCount,
                viewedCount,
                acceptedCount,
                dismissedCount,
                completedCount,
                expiredCount
        );
    }

    @Override
    public List<AgronomicRecommendation> findExpiredActiveRecommendations(Instant now) {
        return springDataRepository.findExpiredActiveRecommendations(now).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
