package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.CalibrationProposal;

import java.time.Instant;
import java.util.UUID;

public record CalibrationProposalDto(
        UUID id,
        String proposalCode,
        String predictionType,
        String currentVersionTag,
        String candidateVersionTag,
        String status,
        String reason,
        String evidence,
        String candidateChanges,
        String proposedConfigurationJson,
        String expectedEffect,
        String riskLevel,
        String affectedScope,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String reviewedBy,
        Instant reviewedAt,
        String reviewNotes
) {
    public static CalibrationProposalDto fromDomain(CalibrationProposal p) {
        if (p == null) return null;
        return new CalibrationProposalDto(
                p.getId(),
                p.getProposalCode(),
                p.getPredictionType().name(),
                p.getCurrentVersionTag(),
                p.getCandidateVersionTag(),
                p.getStatus().name(),
                p.getReason(),
                p.getEvidence(),
                p.getCandidateChanges(),
                p.getProposedConfigurationJson(),
                p.getExpectedEffect(),
                p.getRiskLevel().name(),
                p.getAffectedScope() != null ? p.getAffectedScope() : "ALL_PARCELS",
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getCreatedBy(),
                p.getReviewedBy(),
                p.getReviewedAt(),
                p.getReviewNotes()
        );
    }
}
