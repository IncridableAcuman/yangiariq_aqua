package com.aquaai.risk.domain.port;

import com.aquaai.risk.domain.model.RiskSeverity;

import java.util.UUID;

public interface RiskNotificationPort {

    void sendRiskAlert(
            UUID assessmentId,
            UUID fieldId,
            String fieldName,
            RiskSeverity severity,
            String title,
            String message,
            boolean isSimulated
    );
}
