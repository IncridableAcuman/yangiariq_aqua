package com.aquaai.risk.infrastructure.notification;

import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.port.RiskNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-app notification adapter for high-priority risk alerts.
 * Enforces deduplication and simulation isolation.
 */
@Slf4j
@Component
public class InAppRiskNotificationAdapter implements RiskNotificationPort {

    // Simple in-memory deduplication cache: fieldId:title -> lastDispatchedInstant
    private final Map<String, Instant> deduplicationCache = new ConcurrentHashMap<>();

    @Override
    public void sendRiskAlert(
            UUID assessmentId,
            UUID fieldId,
            String fieldName,
            RiskSeverity severity,
            String title,
            String message,
            boolean isSimulated
    ) {
        if (severity != RiskSeverity.CRITICAL && severity != RiskSeverity.HIGH) {
            return;
        }

        if (isSimulated) {
            log.info("[SIMULATION ALERT] Suppressing farmer broadcast for simulated risk alert: fieldId={}, severity={}, title='{}'",
                    fieldId, severity, title);
            return;
        }

        String dedupKey = fieldId + ":" + title;
        Instant now = Instant.now();
        Instant lastSent = deduplicationCache.get(dedupKey);
        if (lastSent != null && now.minusSeconds(1800).isBefore(lastSent)) {
            // Deduplicated: suppress duplicate notifications within 30 minutes
            log.debug("[RISK NOTIFICATION DEDUP] Suppressed alert within cooldown window: key='{}'", dedupKey);
            return;
        }

        deduplicationCache.put(dedupKey, now);
        log.info("[PRODUCTION RISK ALERT] Dispatching risk notice: assessmentId={}, field='{}' ({}), severity={}, title='{}', message='{}'",
                assessmentId, fieldName, fieldId, severity, title, message);
    }
}
