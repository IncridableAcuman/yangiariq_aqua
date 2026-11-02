package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Builder
public record FieldEvaluationDto(
        UUID fieldId,
        String fieldName,
        String cropType,
        BigDecimal areaHectares,
        int totalPredictions,
        int evaluatedCount,
        boolean isSufficientSample,
        BigDecimal meanAbsoluteError,
        BigDecimal accuracyPercent,
        EvaluationStatus evaluationStatus,
        ModelHealthStatus healthStatus,
        Map<String, Integer> dataQualityBreakdown
) {}
