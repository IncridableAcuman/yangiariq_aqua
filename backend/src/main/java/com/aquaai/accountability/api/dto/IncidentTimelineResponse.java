package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;

import java.time.Instant;
import java.util.UUID;

public record IncidentTimelineResponse(
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
    public static IncidentTimelineResponse from(IncidentTimelineEntry t) {
        if (t == null) return null;
        return new IncidentTimelineResponse(
                t.getId(),
                t.getIncidentId(),
                t.getEventType(),
                t.getActorId(),
                t.getActorName(),
                t.getActorRole(),
                t.getTimestamp(),
                t.getPreviousState(),
                t.getNewState(),
                t.getReason(),
                t.getCorrelationId(),
                t.getMetadataJson()
        );
    }
}
