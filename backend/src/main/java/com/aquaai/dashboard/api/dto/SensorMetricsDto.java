package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SensorMetricsDto {
    private final int totalSensors;
    private final int activeSensors;
    private final int offlineSensors;
    private final int maintenanceSensors;
    private final int staleTelemetrySensors;
    private final BigDecimal validTelemetryPercentage;
}
