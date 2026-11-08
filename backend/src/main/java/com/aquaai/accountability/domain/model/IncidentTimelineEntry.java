package com.aquaai.accountability.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable timeline and audit log entry capturing every transition and administrative action.
 */
@Getter
@Builder(toBuilder = true)
public class IncidentTimelineEntry {

    private final UUID id;
    private final UUID incidentId;
    private final String eventType;
    private final UUID actorId;
    private final String actorName;
    private final String actorRole;
    private final Instant timestamp;
    private final IncidentStatus previousState;
    private final IncidentStatus newState;
    private final String reason;
    private final String correlationId;
    private final String metadataJson;

    public IncidentTimelineEntry(
            UUID id,
            UUID incidentId,
            String eventType,
            UUID actorId,
            String actorName,
            String actorRole,
            Instant timestamp,
            IncidentStatus previousState,
            IncidentStatus newState,
            String reason,
            String correlationId,
            String metadataJson
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.incidentId = Objects.requireNonNull(incidentId, "Incident ID cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
        this.actorId = actorId;
        this.actorName = actorName != null ? actorName : "System";
        this.actorRole = actorRole;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.previousState = previousState;
        this.newState = newState;
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null").trim();
        this.correlationId = correlationId;
        this.metadataJson = metadataJson;
    }
}
