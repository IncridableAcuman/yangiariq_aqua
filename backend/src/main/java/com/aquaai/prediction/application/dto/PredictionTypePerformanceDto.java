package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionType;
import lombok.Builder;

import java.util.List;

@Builder
public record PredictionTypePerformanceDto(
        PredictionType predictionType,
        String displayName,
        String category, // NUMERICAL or CLASSIFICATION
        int sampleSize,
        boolean isSufficientSample,
        NumericalEvaluationMetrics numericalMetrics,
        ClassificationEvaluationMetrics classificationMetrics,
        ModelHealthStatus healthStatus,
        List<String> healthReasons,
        List<PredictionEvaluationDetailDto> largestDeviations
) {}
