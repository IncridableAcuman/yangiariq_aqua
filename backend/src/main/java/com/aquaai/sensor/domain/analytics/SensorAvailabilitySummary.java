package com.aquaai.sensor.domain.analytics;

/**
 * Summary of hardware lifecycle and communication availability for sensors deployed across a parcel.
 */
public record SensorAvailabilitySummary(
        int totalSensors,
        int activeCount,
        int inactiveCount,
        int maintenanceCount,
        int offlineCount,
        int communicatingCount,
        int unresponsiveCount
) {

    public SensorAvailabilitySummary {
        if (totalSensors < 0 || activeCount < 0 || inactiveCount < 0 || maintenanceCount < 0 || offlineCount < 0) {
            throw new IllegalArgumentException("Sensor counts cannot be negative");
        }
    }

    public static SensorAvailabilitySummary empty() {
        return new SensorAvailabilitySummary(0, 0, 0, 0, 0, 0, 0);
    }
}
