package com.aquaai.anomaly.domain.detection;

import com.aquaai.anomaly.domain.model.AnomalyCandidate;
import com.aquaai.sensor.domain.model.SensorType;

import java.util.List;

/**
 * Pluggable detection strategy SPI.
 * Evaluates contextual telemetry and generates explainable anomaly candidates.
 */
public interface AnomalyDetector {

    /**
     * Indicates whether this detector evaluates observations of the given sensor modality.
     */
    boolean supports(SensorType sensorType);

    /**
     * Executes anomaly detection against the provided context.
     * Must avoid database access or side-effects.
     */
    List<AnomalyCandidate> detect(DetectionContext context);
}
