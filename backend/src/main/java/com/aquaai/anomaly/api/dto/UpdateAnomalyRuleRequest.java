package com.aquaai.anomaly.api.dto;

import com.aquaai.anomaly.domain.model.AnomalySeverity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateAnomalyRuleRequest(
        @NotBlank @Size(max = 128)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        AnomalySeverity severity,

        @NotNull
        BigDecimal threshold,

        BigDecimal secondaryThreshold,

        @DecimalMin("1")
        Integer timeWindowMinutes,

        @DecimalMin("1")
        Integer minConsecutiveReadings
) {
}
