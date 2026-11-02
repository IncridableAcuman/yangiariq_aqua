package com.aquaai.prediction.application.dto;

public record ActivateVersionRequest(
        String rolloutMode,
        String reason
) {
}
