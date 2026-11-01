package com.aquaai.intelligence.application.dto;

import com.aquaai.intelligence.domain.model.AgronomicSignal;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;

import java.math.BigDecimal;
import java.util.UUID;

public record AgronomicSignalResponse(
        UUID id,
        AgronomicSignalType signalType,
        IntelligenceSeverity severity,
        String metric,
        BigDecimal observedValue,
        BigDecimal thresholdValue,
        String message
) {
    public static AgronomicSignalResponse fromDomain(AgronomicSignal domain) {
        if (domain == null) return null;
        return new AgronomicSignalResponse(
                domain.getId(),
                domain.getSignalType(),
                domain.getSeverity(),
                domain.getMetric(),
                domain.getObservedValue(),
                domain.getThresholdValue(),
                domain.getMessage()
        );
    }
}
