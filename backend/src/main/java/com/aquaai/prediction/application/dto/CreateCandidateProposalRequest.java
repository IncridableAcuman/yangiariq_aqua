package com.aquaai.prediction.application.dto;

public record CreateCandidateProposalRequest(
        String predictionType,
        String reason,
        String candidateChanges,
        String proposedConfigurationJson,
        String expectedEffect,
        String riskLevel
) {
}
