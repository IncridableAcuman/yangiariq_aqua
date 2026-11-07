package com.aquaai.network.domain.port;

import java.util.UUID;

public interface NetworkNotificationPort {
    void sendDispatchNotification(
            UUID dispatchScheduleId,
            String title,
            String message,
            String severity // CRITICAL, HIGH, INFO
    );
}
