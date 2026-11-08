package com.aquaai.performance.infrastructure.persistence.repository;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.infrastructure.persistence.entity.ImpactAssessmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataImpactAssessmentRepository extends JpaRepository<ImpactAssessmentJpaEntity, UUID> {
    List<ImpactAssessmentJpaEntity> findByInterventionType(String interventionType);
    List<ImpactAssessmentJpaEntity> findByAttributionLevel(AttributionLevel attributionLevel);
    List<ImpactAssessmentJpaEntity> findByReviewStatus(AssessmentReviewStatus reviewStatus);
    List<ImpactAssessmentJpaEntity> findByScopeId(UUID scopeId);
}
