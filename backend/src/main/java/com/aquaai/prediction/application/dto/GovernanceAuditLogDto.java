package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.GovernanceAuditLog;

import java.time.Instant;
import java.util.UUID;

public record GovernanceAuditLogDto(
        UUID id,
        String action,
        String predictionType,
        String versionTag,
        UUID proposalId,
        String actorId,
        String actorRole,
        Instant timestamp,
        String reason,
        String previousState,
        String newState,
        String detailsJson
) {
    public static GovernanceAuditLogDto fromDomain(GovernanceAuditLog a) {
        if (a == null) return null;
        return new GovernanceAuditLogDto(
                a.getId(),
                a.getAction().name(),
                a.getPredictionType().name(),
                a.getVersionTag(),
                a.getProposalId(),
                a.getActorId(),
                a.getActorRole(),
                a.getTimestamp(),
                a.getReason(),
                a.getPreviousState(),
                a.getNewState(),
                a.getDetailsJson()
        );
    }
}
