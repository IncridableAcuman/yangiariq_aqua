package com.aquaai.risk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveAssessmentRequest(
        @NotBlank(message = "Resolution notes are required")
        @Size(max = 1000, message = "Resolution notes cannot exceed 1000 characters")
        String resolutionNotes
) {
}
