package com.aquaai.pilot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PilotParticipant {
    private final UUID id;
    private final UUID pilotId;
    private final UUID userId;
    private String roleInPilot;
    private String assignedFieldIds;
    private final Instant joinedAt;

    public PilotParticipant(
            UUID id,
            UUID pilotId,
            UUID userId,
            String roleInPilot,
            String assignedFieldIds,
            Instant joinedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.pilotId = Objects.requireNonNull(pilotId, "pilotId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.roleInPilot = Objects.requireNonNull(roleInPilot, "roleInPilot must not be null");
        this.assignedFieldIds = assignedFieldIds;
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
    }

    public static PilotParticipant create(UUID pilotId, UUID userId, String roleInPilot, String assignedFieldIds) {
        return new PilotParticipant(UUID.randomUUID(), pilotId, userId, roleInPilot, assignedFieldIds, Instant.now());
    }

    public void updateRole(String roleInPilot, String assignedFieldIds) {
        this.roleInPilot = Objects.requireNonNull(roleInPilot, "roleInPilot must not be null");
        this.assignedFieldIds = assignedFieldIds;
    }

    public UUID getId() { return id; }
    public UUID getPilotId() { return pilotId; }
    public UUID getUserId() { return userId; }
    public String getRoleInPilot() { return roleInPilot; }
    public String getAssignedFieldIds() { return assignedFieldIds; }
    public Instant getJoinedAt() { return joinedAt; }
}
