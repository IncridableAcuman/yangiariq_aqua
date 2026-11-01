package com.aquaai.intelligence.domain.port;

import java.util.UUID;

/**
 * Domain port for querying active anomaly detection state on a parcel.
 */
public interface IntelligenceAnomalyPort {

    record ActiveAnomalyStatus(
            boolean hasPipeBurst,
            boolean hasCriticalPressureDrop,
            int activeAnomaliesCount
    ) {}

    ActiveAnomalyStatus getActiveAnomalyStatus(UUID fieldId);
}
