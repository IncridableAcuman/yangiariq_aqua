package com.aquaai.prediction.application.dto;

import lombok.Builder;

@Builder
public record EvaluateBatchResponse(
        int processedCount,
        int evaluatedCount,
        int insufficientDataCount,
        int interruptedCount,
        String message
) {}
