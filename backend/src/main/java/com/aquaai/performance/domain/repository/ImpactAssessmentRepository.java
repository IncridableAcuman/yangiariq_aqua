package com.aquaai.performance.domain.repository;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.ImpactAssessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImpactAssessmentRepository {
    List<ImpactAssessment> findAll();
    Optional<ImpactAssessment> findById(UUID id);
    List<ImpactAssessment> findByInterventionType(String interventionType);
    List<ImpactAssessment> findByAttributionLevel(AttributionLevel attributionLevel);
    List<ImpactAssessment> findByReviewStatus(AssessmentReviewStatus status);
    List<ImpactAssessment> findByScopeId(UUID scopeId);
    ImpactAssessment save(ImpactAssessment assessment);
    void saveAll(List<ImpactAssessment> assessments);
}
