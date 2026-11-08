package com.aquaai.performance.api.dto;

import com.aquaai.performance.application.dto.PerformanceTrendDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PerformanceTrendResponse {
    private final String indicatorCode;
    private final String indicatorName;
    private final String measurementUnit;
    private final List<PerformanceTrendDto.DataPoint> dataPoints;
}
