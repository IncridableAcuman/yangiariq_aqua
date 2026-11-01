package com.aquaai.recommendation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DismissRecommendationRequest(
        @NotBlank(message = "Dismissal reason is mandatory")
        @Size(max = 500, message = "Dismissal reason must not exceed 500 characters")
        String reason
) {
}
