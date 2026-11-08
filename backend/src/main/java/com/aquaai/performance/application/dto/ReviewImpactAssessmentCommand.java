package com.aquaai.performance.application.dto;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewImpactAssessmentCommand {
    private final UUID assessmentId;
    private final AssessmentReviewStatus reviewStatus;
    private final UUID reviewerId;
    private final String reviewerName;
    private final String reviewNotes;
}
