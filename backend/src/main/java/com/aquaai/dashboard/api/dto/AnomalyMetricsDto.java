package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnomalyMetricsDto {
    private final int totalOpen;
    private final int critical;
    private final int high;
    private final int medium;
    private final int acknowledged;
}
