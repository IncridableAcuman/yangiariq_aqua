package com.aquaai.anomaly.infrastructure.notification;

import com.aquaai.anomaly.application.port.AnomalyNotificationPort;
import com.aquaai.anomaly.infrastructure.config.AnomalyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-app notification adapter. Dispatches notifications with simulation isolation
 * (Refinement #5: simulated telemetry alerts are suppressed from farmer production channels).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppAnomalyNotificationAdapter implements AnomalyNotificationPort {

    private final AnomalyProperties properties;

    @Override
    public void sendAlert(AnomalyNotification notification) {
        if (!properties.isNotificationEnabled()) {
            return;
        }

        // Refinement #5: Simulation isolation
        if (notification.isSimulated()) {
            if (!properties.isSimulationAlertsEnabled()) {
                log.debug("[SIMULATION ISOLATION] Suppressed alert dispatch for simulated anomaly: anomalyId={}, type={}",
                        notification.anomalyId(), notification.anomalyType());
                return;
            }
            log.info("[SIMULATION ALERT] Simulated anomaly notice: fieldId={}, type={}, severity={}",
                    notification.fieldId(), notification.anomalyType(), notification.severity());
            return;
        }

        log.info("[PRODUCTION ALERT DISPATCH] Anomaly alert generated: anomalyId={}, fieldId={}, type={}, severity={}, title='{}'",
                notification.anomalyId(), notification.fieldId(), notification.anomalyType(), notification.severity(), notification.title());
    }
}
