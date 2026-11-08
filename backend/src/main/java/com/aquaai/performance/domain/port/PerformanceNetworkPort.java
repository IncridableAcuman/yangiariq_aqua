package com.aquaai.performance.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PerformanceNetworkPort {

    record DispatchDeliverySummary(
            UUID dispatchScheduleId,
            UUID fieldId,
            String fieldName,
            BigDecimal plannedVolumeM3,
            BigDecimal actualVolumeM3,
            BigDecimal varianceVolumeM3,
            BigDecimal variancePercent
    ) {}

    record NetworkConveyanceMetric(
            long totalSchedules,
            long executedSchedules,
            BigDecimal totalPlannedDeliveryM3,
            BigDecimal totalActualDeliveryM3,
            BigDecimal averageFulfillmentPct
    ) {}

    NetworkConveyanceMetric getDispatchDeliveryMetrics();
    List<DispatchDeliverySummary> getRecentDeliveries();
}
