package com.aquaai.network.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record DispatchOptimizationRequest(
        UUID targetWuaId,
        @NotNull Instant periodStart,
        @NotNull Instant periodEnd,
        String fairnessPolicyType, // CROP_CRITICALITY_FIRST, MOISTURE_DEFICIT_PRIORITY, PROPORTIONAL_DEFICIT
        String notes
) {}
