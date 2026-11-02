package com.aquaai.prediction.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable security and operational audit trail for predictive model governance events.
 */
public class GovernanceAuditLog {

    private final UUID id;
    private final GovernanceAction action;
    private final PredictionType predictionType;
    private final String versionTag;
    private final UUID proposalId;
    private final String actorId;
    private final String actorRole;
    private final Instant timestamp;
    private final String reason;
    private final String previousState;
    private final String newState;
    private final String detailsJson;

    public GovernanceAuditLog(
            UUID id,
            GovernanceAction action,
            PredictionType predictionType,
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
        this.id = id != null ? id : UUID.randomUUID();
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.versionTag = versionTag;
        this.proposalId = proposalId;
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
        this.actorRole = Objects.requireNonNull(actorRole, "actorRole must not be null");
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.previousState = previousState;
        this.newState = newState;
        this.detailsJson = detailsJson != null ? detailsJson : "{}";
    }

    public static GovernanceAuditLog create(
            GovernanceAction action,
            PredictionType predictionType,
            String versionTag,
            UUID proposalId,
            String actorId,
            String actorRole,
            String reason,
            String previousState,
            String newState,
            String detailsJson
    ) {
        return new GovernanceAuditLog(
                UUID.randomUUID(),
                action,
                predictionType,
                versionTag,
                proposalId,
                actorId,
                actorRole,
                Instant.now(),
                reason,
                previousState,
                newState,
                detailsJson
        );
    }

    // Getters
    public UUID getId() { return id; }
    public GovernanceAction getAction() { return action; }
    public PredictionType getPredictionType() { return predictionType; }
    public String getVersionTag() { return versionTag; }
    public UUID getProposalId() { return proposalId; }
    public String getActorId() { return actorId; }
    public String getActorRole() { return actorRole; }
    public Instant getTimestamp() { return timestamp; }
    public String getReason() { return reason; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    public String getDetailsJson() { return detailsJson; }
}
