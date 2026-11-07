package com.aquaai.wateraccounting.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReconciliationReviewRequest(
        @NotBlank(message = "Review notes are mandatory")
        @Size(max = 1000, message = "Review notes must not exceed 1000 characters")
        String notes
) {}
