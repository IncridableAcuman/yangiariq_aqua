package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.ReviewImpactAssessmentCommand;
import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewImpactAssessmentUseCase {

    private final ImpactAssessmentRepository impactAssessmentRepository;

    @Transactional
    public ImpactAssessment execute(ReviewImpactAssessmentCommand command) {
        ImpactAssessment existing = impactAssessmentRepository.findById(command.getAssessmentId())
                .orElseThrow(() -> new NoSuchElementException("Impact Assessment not found: " + command.getAssessmentId()));

        AttributionLevel updatedAttribution = existing.getAttributionLevel();
        if (command.getReviewStatus() == AssessmentReviewStatus.VERIFIED_OUTCOME) {
            // If verified by independent reviewer and no major confounders, can promote to DIRECTLY_VERIFIED
            if (existing.getConfoundingFactors() == null || existing.getConfoundingFactors().isBlank()) {
                updatedAttribution = AttributionLevel.DIRECTLY_VERIFIED;
            }
        } else if (command.getReviewStatus() == AssessmentReviewStatus.INCONCLUSIVE) {
            updatedAttribution = AttributionLevel.INCONCLUSIVE;
        }

        ImpactAssessment updated = existing.toBuilder()
                .reviewStatus(command.getReviewStatus())
                .attributionLevel(updatedAttribution)
                .reviewerId(command.getReviewerId())
                .reviewerName(command.getReviewerName())
                .reviewNotes(command.getReviewNotes())
                .reviewedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return impactAssessmentRepository.save(updated);
    }
}
