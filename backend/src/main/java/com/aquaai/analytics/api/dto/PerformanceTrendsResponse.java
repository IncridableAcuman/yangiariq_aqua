package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.TrendGranularity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PerformanceTrendsResponse {
    private final TrendGranularity granularity;
    private final List<PerformanceTrendPointDto> trendPoints;
}
