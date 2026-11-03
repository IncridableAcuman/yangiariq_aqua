package com.aquaai.prediction.domain.model;

/**
 * Status of hardware sensor availability on the parcel.
 */
public enum SensorAvailabilityStatus {
    COMPLETE, // All expected sensors are online and delivering observations
    PARTIAL,  // Some sensors offline or in maintenance
    MISSING,  // No active sensors registered or transmitting on parcel
    UNKNOWN
}
