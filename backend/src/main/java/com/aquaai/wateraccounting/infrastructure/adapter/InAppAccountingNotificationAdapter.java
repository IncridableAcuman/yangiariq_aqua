package com.aquaai.wateraccounting.infrastructure.adapter;

import com.aquaai.wateraccounting.domain.port.AccountingNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InAppAccountingNotificationAdapter implements AccountingNotificationPort {

    private final Map<String, Instant> deduplicationCache = new ConcurrentHashMap<>();

    @Override
    public void sendReconciliationAlert(UUID reconciliationId, String title, String message, String severity) {
        String dedupKey = reconciliationId + ":" + title;
        Instant now = Instant.now();
        Instant lastSent = deduplicationCache.get(dedupKey);
        if (lastSent != null && now.minusSeconds(900).isBefore(lastSent)) {
            log.debug("[ACCOUNTING NOTIFICATION DEDUP] Suppressed alert within cooldown window: key='{}'", dedupKey);
            return;
        }

        deduplicationCache.put(dedupKey, now);
        log.info("[WATER ACCOUNTING ALERT] severity='{}', reconciliationId={}, title='{}', message='{}'",
                severity, reconciliationId, title, message);
    }
}
