package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationMetricsDto {
    private final int totalActive;
    private final int critical;
    private final int high;
    private final int accepted;
}
