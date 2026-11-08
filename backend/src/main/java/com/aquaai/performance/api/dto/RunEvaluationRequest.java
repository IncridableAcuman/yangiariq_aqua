package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.GeographicScopeType;
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
public class RunEvaluationRequest {

    @NotBlank(message = "Indicator code is required")
    private String indicatorCode;

    @NotNull(message = "Scope type is required")
    private GeographicScopeType scopeType;

    private UUID scopeId;

    private Instant periodStart;
    private Instant periodEnd;

    private BigDecimal customValue;
    private String calculationVersion;
}
