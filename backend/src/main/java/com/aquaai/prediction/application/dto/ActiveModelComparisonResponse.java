package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.PredictionType;

import java.util.UUID;

public record ActiveModelComparisonResponse(
        PredictionType predictionType,
        String activeModelVersion,
        String activeModelType,
        String governanceStatus,
        String baselineModelVersion,
        Double activeMae,
        Double baselineMae,
        Double relativeImprovementPercent,
        int sampleSize,
        String contextHealth,
        String dataQualityStatus,
        boolean isHybridActive,
        String summaryRationale
) {}
