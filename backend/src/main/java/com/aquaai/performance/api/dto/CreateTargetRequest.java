package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetType;
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
public class CreateTargetRequest {

    @NotBlank(message = "Indicator code is required")
    private String indicatorCode;

    @NotBlank(message = "Target title is required")
    private String title;

    @NotNull(message = "Scope type is required")
    private GeographicScopeType scopeType;

    private UUID scopeId;

    @NotBlank(message = "Scope name is required")
    private String scopeName;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    private TargetCategory targetCategory;

    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    private BigDecimal rangeMin;
    private BigDecimal rangeMax;

    @NotNull(message = "Measurement unit is required")
    private MeasurementUnit measurementUnit;

    @NotNull(message = "Effective start is required")
    private Instant effectiveStart;

    private Instant effectiveEnd;

    @NotBlank(message = "Target owner is required")
    private String targetOwner;

    private String rationale;
}
