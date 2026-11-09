package com.aquaai.integration.application.dto;

import java.util.List;

public record IngestionResultDto(
        int totalReceived,
        int validCount,
        int rejectedCount,
        int duplicateCount,
        List<String> errors
) {
    public static IngestionResultDto of(int total, int valid, int rejected, int duplicate, List<String> errors) {
        return new IngestionResultDto(total, valid, rejected, duplicate, errors != null ? errors : List.of());
    }
}
