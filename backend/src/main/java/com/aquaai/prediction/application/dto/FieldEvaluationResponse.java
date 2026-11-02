package com.aquaai.prediction.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FieldEvaluationResponse(
        List<FieldEvaluationDto> fields,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
