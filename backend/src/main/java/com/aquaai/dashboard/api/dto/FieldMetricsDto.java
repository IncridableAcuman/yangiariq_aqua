package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FieldMetricsDto {
    private final int totalFields;
    private final int activeFields;
    private final int inactiveFields;
    private final int fieldsRequiringAttention;
    private final BigDecimal totalAreaHectares;
}
