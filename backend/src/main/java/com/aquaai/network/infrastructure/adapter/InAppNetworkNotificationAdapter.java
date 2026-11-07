package com.aquaai.network.infrastructure.adapter;

import com.aquaai.network.domain.port.NetworkNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-app notification adapter for canal dispatch and network resilience alerts.
 */
@Slf4j
@Component
public class InAppNetworkNotificationAdapter implements NetworkNotificationPort {

    private final Map<String, Instant> deduplicationCache = new ConcurrentHashMap<>();

    @Override
    public void sendDispatchNotification(
            UUID dispatchScheduleId,
            String title,
            String message,
            String severity
    ) {
        String dedupKey = dispatchScheduleId + ":" + title;
        Instant now = Instant.now();
        Instant lastSent = deduplicationCache.get(dedupKey);
        if (lastSent != null && now.minusSeconds(900).isBefore(lastSent)) {
            log.debug("[NETWORK NOTIFICATION DEDUP] Suppressed alert within cooldown window: key='{}'", dedupKey);
            return;
        }

        deduplicationCache.put(dedupKey, now);
        log.info("[NETWORK NOTIFICATION] severity='{}', scheduleId={}, title='{}', message='{}'",
                severity, dispatchScheduleId, title, message);
    }
}
