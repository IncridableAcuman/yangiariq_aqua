package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBaselineRequest {

    @NotBlank(message = "Indicator code is required")
    private String indicatorCode;

    @NotBlank(message = "Baseline name is required")
    private String name;

    private String description;

    @NotNull(message = "Scope type is required")
    private GeographicScopeType scopeType;

    private UUID scopeId;

    @NotBlank(message = "Scope name is required")
    private String scopeName;

    @NotNull(message = "Period start is required")
    private Instant periodStart;

    @NotNull(message = "Period end is required")
    private Instant periodEnd;

    @NotNull(message = "Baseline value is required")
    private BigDecimal baselineValue;

    @NotNull(message = "Measurement unit is required")
    private MeasurementUnit measurementUnit;

    @DecimalMin("0.0")
    private BigDecimal dataCompletenessPct;

    private EvidenceClassification evidenceClassification;

    private BaselineQuality baselineQuality;

    private String calculationMethod;

    private String seasonContext;
}
