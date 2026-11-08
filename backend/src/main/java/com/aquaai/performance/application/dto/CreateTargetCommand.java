package com.aquaai.performance.application.dto;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CreateTargetCommand {
    private final String indicatorCode;
    private final String title;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final TargetType targetType;
    private final TargetCategory targetCategory;
    private final BigDecimal targetValue;
    private final BigDecimal rangeMin;
    private final BigDecimal rangeMax;
    private final MeasurementUnit measurementUnit;
    private final Instant effectiveStart;
    private final Instant effectiveEnd;
    private final String targetOwner;
    private final String rationale;
}
