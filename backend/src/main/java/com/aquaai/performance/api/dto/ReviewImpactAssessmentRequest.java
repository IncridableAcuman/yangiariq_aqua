package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImpactAssessmentRequest {

    @NotNull(message = "Review status is required")
    private AssessmentReviewStatus reviewStatus;

    private String reviewNotes;
}
