package com.aquaai.prediction.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record EvaluationHistoryResponse(
        List<PredictionEvaluationDetailDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
