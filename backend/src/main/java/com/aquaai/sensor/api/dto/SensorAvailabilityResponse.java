package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;

public record SensorAvailabilityResponse(
        int totalSensors,
        int activeCount,
        int inactiveCount,
        int maintenanceCount,
        int offlineCount,
        int communicatingCount,
        int unresponsiveCount
) {

    public static SensorAvailabilityResponse from(SensorAvailabilitySummary s) {
        if (s == null) {
            return null;
        }
        return new SensorAvailabilityResponse(
                s.totalSensors(),
                s.activeCount(),
                s.inactiveCount(),
                s.maintenanceCount(),
                s.offlineCount(),
                s.communicatingCount(),
                s.unresponsiveCount()
        );
    }
}
