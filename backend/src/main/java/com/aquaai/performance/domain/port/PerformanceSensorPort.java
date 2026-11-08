package com.aquaai.performance.domain.port;

import java.math.BigDecimal;

public interface PerformanceSensorPort {

    record SensorQualitySummary(
            long totalReadings,
            long observedReadings,
            long simulatedReadings,
            long estimatedReadings,
            long missingIntervals,
            BigDecimal observedCoveragePct,
            long activeSensorsCount,
            long degradedSensorsCount
    ) {}

    SensorQualitySummary getRegionalDataQualitySummary();
}
