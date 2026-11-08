package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.IncidentSeverity;

import java.util.UUID;

public interface AccountabilityNotificationPort {

    void sendIncidentAlert(
            UUID incidentId,
            String incidentNumber,
            IncidentSeverity severity,
            String title,
            String message,
            boolean isSimulated
    );

    void sendSlaBreachAlert(
            UUID incidentId,
            String incidentNumber,
            String title,
            String message
    );
}
