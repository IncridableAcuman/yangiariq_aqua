package com.aquaai.recommendation.infrastructure.persistence;

import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRecommendationRepository extends JpaRepository<RecommendationJpaEntity, UUID> {

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE r.fieldId = :fieldId " +
            "AND r.recommendationType = :recommendationType " +
            "AND r.status IN ('GENERATED', 'VIEWED', 'ACCEPTED')")
    Optional<RecommendationJpaEntity> findActiveRecommendation(
            @Param("fieldId") UUID fieldId,
            @Param("recommendationType") RecommendationType recommendationType
    );

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE r.fieldId = :fieldId " +
            "AND r.status IN ('GENERATED', 'VIEWED', 'ACCEPTED') " +
            "ORDER BY r.generatedAt DESC")
    List<RecommendationJpaEntity> findActiveByFieldId(@Param("fieldId") UUID fieldId);

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE r.fieldId IN :fieldIds " +
            "AND r.status IN ('GENERATED', 'VIEWED', 'ACCEPTED') " +
            "ORDER BY r.generatedAt DESC")
    List<RecommendationJpaEntity> findActiveByFieldIdIn(@Param("fieldIds") java.util.Collection<UUID> fieldIds);

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE r.fieldId = :fieldId ORDER BY r.generatedAt DESC")
    Page<RecommendationJpaEntity> findByFieldId(@Param("fieldId") UUID fieldId, Pageable pageable);

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE " +
            "(:fieldId IS NULL OR r.fieldId = :fieldId) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:priority IS NULL OR r.priority = :priority) AND " +
            "(:recommendationType IS NULL OR r.recommendationType = :recommendationType) AND " +
            "(cast(:from as timestamp) IS NULL OR r.generatedAt >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR r.generatedAt <= :to)")
    Page<RecommendationJpaEntity> findWithFilters(
            @Param("fieldId") UUID fieldId,
            @Param("status") RecommendationStatus status,
            @Param("priority") RecommendationPriority priority,
            @Param("recommendationType") RecommendationType recommendationType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("SELECT r FROM RecommendationJpaEntity r WHERE " +
            "r.status IN ('GENERATED', 'VIEWED', 'ACCEPTED') AND " +
            "r.validUntil < :now")
    List<RecommendationJpaEntity> findExpiredActiveRecommendations(@Param("now") Instant now);

    @Query("SELECT COUNT(r) FROM RecommendationJpaEntity r WHERE " +
            "(:fieldId IS NULL OR r.fieldId = :fieldId) AND " +
            "r.status IN ('GENERATED', 'VIEWED', 'ACCEPTED')")
    long countActive(@Param("fieldId") UUID fieldId);

    @Query("SELECT COUNT(r) FROM RecommendationJpaEntity r WHERE " +
            "(:fieldId IS NULL OR r.fieldId = :fieldId) AND " +
            "r.priority = :priority")
    long countByPriority(
            @Param("fieldId") UUID fieldId,
            @Param("priority") RecommendationPriority priority
    );

    @Query("SELECT COUNT(r) FROM RecommendationJpaEntity r WHERE " +
            "(:fieldId IS NULL OR r.fieldId = :fieldId) AND " +
            "r.status = :status")
    long countByStatus(
            @Param("fieldId") UUID fieldId,
            @Param("status") RecommendationStatus status
    );
}
