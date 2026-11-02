package com.aquaai.prediction.application.dto;

public record RollbackVersionRequest(
        String priorVersionTag,
        String reason
) {
}
