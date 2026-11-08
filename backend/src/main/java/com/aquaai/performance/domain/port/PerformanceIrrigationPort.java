package com.aquaai.performance.domain.port;

import java.math.BigDecimal;

public interface PerformanceIrrigationPort {

    record IrrigationPerformanceSummary(
            long totalPlans,
            long completedPlans,
            long approvedPlans,
            long cancelledPlans,
            BigDecimal scheduleAdherencePct,
            BigDecimal averageDelayHours
    ) {}

    IrrigationPerformanceSummary getIrrigationPerformanceSummary();
}
