package com.aquaai.performance.application.dto;

import com.aquaai.performance.domain.model.GeographicScopeType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RunEvaluationCommand {
    private final String indicatorCode;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal customValue; // optional manual or computed override
    private final String calculationVersion;
}
