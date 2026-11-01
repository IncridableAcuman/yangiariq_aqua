package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyRuleResponse(
        UUID id,
        String ruleCode,
        SensorType sensorType,
        String name,
        String description,
        boolean enabled,
        AnomalySeverity severity,
        BigDecimal threshold,
        BigDecimal secondaryThreshold,
        int timeWindowMinutes,
        int minConsecutiveReadings,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnomalyRuleResponse fromDomain(AnomalyRuleConfig domain) {
        if (domain == null) return null;
        return new AnomalyRuleResponse(
                domain.getId(),
                domain.getRuleCode(),
                domain.getSensorType(),
                domain.getName(),
                domain.getDescription(),
                domain.isEnabled(),
                domain.getSeverity(),
                domain.getThreshold(),
                domain.getSecondaryThreshold(),
                domain.getTimeWindowMinutes(),
                domain.getMinConsecutiveReadings(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
