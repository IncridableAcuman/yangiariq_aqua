package com.aquaai.irrigation.domain.port;

import java.util.UUID;

/**
 * Domain port for checking active hydraulic and soil anomalies for a parcel.
 */
public interface AnomalyDataPort {

    record ParcelAnomalyStatus(
            boolean hasSuspectedPipeBurst,
            boolean hasCriticalPressureDrop,
            boolean hasCriticalSoilMoistureStress,
            boolean hasSensorDrift
    ) {}

    ParcelAnomalyStatus getAnomalyStatus(UUID fieldId);
}
