package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListImpactAssessmentsUseCase {

    private final ImpactAssessmentRepository impactAssessmentRepository;

    @Transactional(readOnly = true)
    public List<ImpactAssessment> execute(
            String interventionType,
            AttributionLevel attributionLevel,
            AssessmentReviewStatus reviewStatus,
            UUID scopeId
    ) {
        List<ImpactAssessment> all = impactAssessmentRepository.findAll();
        return all.stream()
                .filter(a -> interventionType == null || a.getInterventionType().equalsIgnoreCase(interventionType))
                .filter(a -> attributionLevel == null || a.getAttributionLevel() == attributionLevel)
                .filter(a -> reviewStatus == null || a.getReviewStatus() == reviewStatus)
                .filter(a -> scopeId == null || scopeId.equals(a.getScopeId()))
                .collect(Collectors.toList());
    }
}
