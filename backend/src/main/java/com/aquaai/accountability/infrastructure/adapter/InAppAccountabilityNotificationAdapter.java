package com.aquaai.accountability.infrastructure.adapter;

import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.port.AccountabilityNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-app notification adapter for regional accountability and investigation alerts.
 * Enforces deduplication and simulation isolation.
 */
@Slf4j
@Component
public class InAppAccountabilityNotificationAdapter implements AccountabilityNotificationPort {

    private final Map<String, Instant> deduplicationCache = new ConcurrentHashMap<>();

    @Override
    public void sendIncidentAlert(
            UUID incidentId,
            String incidentNumber,
            IncidentSeverity severity,
            String title,
            String message,
            boolean isSimulated
    ) {
        if (isSimulated) {
            log.info("[SIMULATION INCIDENT] Suppressing broadcast for simulated incident: number={}, title='{}'",
                    incidentNumber, title);
            return;
        }

        String dedupKey = incidentNumber + ":" + title;
        Instant now = Instant.now();
        Instant lastSent = deduplicationCache.get(dedupKey);
        if (lastSent != null && now.minusSeconds(1800).isBefore(lastSent)) {
            log.debug("[ACCOUNTABILITY NOTIFICATION DEDUP] Suppressed alert within cooldown window: key='{}'", dedupKey);
            return;
        }

        deduplicationCache.put(dedupKey, now);
        log.info("[OPERATIONAL INCIDENT ALERT] severity={}, number={}, title='{}', message='{}'",
                severity, incidentNumber, title, message);
    }

    @Override
    public void sendSlaBreachAlert(
            UUID incidentId,
            String incidentNumber,
            String title,
            String message
    ) {
        String dedupKey = "SLA_BREACH:" + incidentNumber;
        Instant now = Instant.now();
        Instant lastSent = deduplicationCache.get(dedupKey);
        if (lastSent != null && now.minusSeconds(3600).isBefore(lastSent)) {
            return;
        }

        deduplicationCache.put(dedupKey, now);
        log.warn("[SLA BREACH ALERT] Overdue incident: number={}, title='{}', message='{}'",
                incidentNumber, title, message);
    }
}
