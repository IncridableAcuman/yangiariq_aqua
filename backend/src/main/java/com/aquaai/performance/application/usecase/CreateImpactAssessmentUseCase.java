package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.CreateImpactAssessmentCommand;
import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.ChangeDirection;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.domain.service.ImpactAttributionEngine;
import com.aquaai.performance.domain.service.IndicatorCalculationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateImpactAssessmentUseCase {

    private final ImpactAssessmentRepository impactAssessmentRepository;
    private final PerformanceIndicatorRepository indicatorRepository;
    private final IndicatorCalculationEngine calculationEngine;
    private final ImpactAttributionEngine attributionEngine;

    @Transactional
    public ImpactAssessment execute(CreateImpactAssessmentCommand command) {
        if (!indicatorRepository.existsByCode(command.getIndicatorCode())) {
            throw new NoSuchElementException("Indicator not found for code: " + command.getIndicatorCode());
        }

        BigDecimal base = command.getBaselineValue();
        BigDecimal post = command.getPostInterventionValue();
        BigDecimal diff = post.subtract(base);
        BigDecimal pctChange = calculationEngine.calculatePercentageChange(post, base);
        ChangeDirection direction = calculationEngine.evaluateChangeDirection(command.getIndicatorCode(), diff);

        // Evaluate conservative attribution
        boolean isFavorable = direction == ChangeDirection.FAVORABLE;
        boolean hasConfounding = command.getConfoundingFactors() != null && !command.getConfoundingFactors().isBlank();

        AttributionLevel level = attributionEngine.determineAttribution(
                command.getBaselineQuality(),
                command.getEvidenceClassification(),
                true,
                false,
                hasConfounding,
                isFavorable,
                false
        );

        Instant now = Instant.now();
        ImpactAssessment assessment = ImpactAssessment.builder()
                .id(UUID.randomUUID())
                .assessmentNumber("IMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .title(command.getTitle())
                .interventionType(command.getInterventionType())
                .interventionId(command.getInterventionId())
                .interventionReference(command.getInterventionReference())
                .interventionDate(command.getInterventionDate())
                .indicatorCode(command.getIndicatorCode())
                .scopeType(command.getScopeType())
                .scopeId(command.getScopeId())
                .scopeName(command.getScopeName())
                .baselineId(command.getBaselineId())
                .baselinePeriodStart(command.getBaselinePeriodStart())
                .baselinePeriodEnd(command.getBaselinePeriodEnd())
                .baselineValue(base)
                .evaluationPeriodStart(command.getEvaluationPeriodStart())
                .evaluationPeriodEnd(command.getEvaluationPeriodEnd())
                .postInterventionValue(post)
                .absoluteChange(diff)
                .percentageChange(pctChange)
                .changeDirection(direction)
                .measurementUnit(command.getMeasurementUnit())
                .evidenceClassification(command.getEvidenceClassification())
                .baselineQuality(command.getBaselineQuality())
                .attributionLevel(command.getAttributionLevel() != null ? command.getAttributionLevel() : level)
                .confoundingFactors(command.getConfoundingFactors())
                .dataLimitations(command.getDataLimitations())
                .comparisonValid(true)
                .methodologyNotes(command.getMethodologyNotes())
                .reviewStatus(AssessmentReviewStatus.SUBMITTED)
                .reviewerId(null)
                .reviewerName(null)
                .reviewNotes(null)
                .reviewedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return impactAssessmentRepository.save(assessment);
    }
}
