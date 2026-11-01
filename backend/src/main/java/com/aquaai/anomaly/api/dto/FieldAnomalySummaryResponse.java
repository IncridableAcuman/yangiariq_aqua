package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.application.usecase.GetFieldAnomalySummaryUseCase;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record FieldAnomalySummaryResponse(
        UUID fieldId,
        long activeCount,
        boolean hasCritical,
        boolean hasHigh,
        List<AnomalyResponse> activeAnomalies
) {
    public static FieldAnomalySummaryResponse fromResult(GetFieldAnomalySummaryUseCase.FieldAnomalySummaryResult result) {
        if (result == null) return null;
        List<AnomalyResponse> anomalyResponses = result.activeAnomalies() != null
                ? result.activeAnomalies().stream().map(AnomalyResponse::fromDomain).toList()
                : Collections.emptyList();

        return new FieldAnomalySummaryResponse(
                result.fieldId(),
                result.activeCount(),
                result.hasCritical(),
                result.hasHigh(),
                anomalyResponses
        );
    }
}
