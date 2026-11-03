package com.aquaai.prediction.domain.model;

/**
 * Normalized active hydraulic and physical anomaly state on the parcel.
 */
public enum AnomalySeverityContext {
    NONE,     // No active anomalies
    LOW,      // Minor warnings (e.g. slight jitter)
    MEDIUM,   // Medium anomaly (e.g. mild flow anomaly)
    HIGH,     // High anomaly (e.g. pressure drop warning)
    CRITICAL, // Critical hydraulic anomaly (e.g. pipe burst, interlock triggered)
    UNKNOWN
}
