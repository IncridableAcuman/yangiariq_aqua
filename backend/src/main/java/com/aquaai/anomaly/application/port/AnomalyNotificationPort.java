package com.aquaai.anomaly.application.port;

import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyType;

import java.time.Instant;
import java.util.UUID;

public interface AnomalyNotificationPort {

    record AnomalyNotification(
            UUID anomalyId,
            UUID fieldId,
            UUID sensorId,
            AnomalyType anomalyType,
            AnomalySeverity severity,
            String title,
            String message,
            boolean isSimulated,
            Instant detectedAt
    ) {
    }

    void sendAlert(AnomalyNotification notification);
}
