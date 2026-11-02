package com.aquaai.analytics.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FieldPerformanceRankingResponse {
    private final List<FieldPerformanceDto> fields;
    private final long totalElements;
    private final int page;
    private final int size;
    private final int totalPages;
}
