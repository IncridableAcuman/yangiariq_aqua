package com.aquaai.integration.application.dto;

public record DataQualitySummaryDto(
        long totalIngested,
        long validCount,
        long invalidCount,
        long duplicateCount,
        long staleCount,
        long outOfRangeCount,
        long unverifiedCount,
        double overallQualityScore
) {}
