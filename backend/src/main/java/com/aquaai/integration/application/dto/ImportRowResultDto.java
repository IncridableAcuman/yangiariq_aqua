package com.aquaai.integration.application.dto;

public record ImportRowResultDto(
        int rowNumber,
        boolean success,
        String externalRecordId,
        String errorMessage
) {}
