package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.MethodVersionStatus;
import jakarta.validation.constraints.NotNull;

public record PromoteCandidateRequest(
        @NotNull MethodVersionStatus targetStatus,
        String reason
) {}
