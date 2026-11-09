package com.aquaai.pilot.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pilot_participants")
public class PilotParticipantEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "pilot_id", nullable = false)
    private UUID pilotId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_in_pilot", nullable = false, length = 64)
    private String roleInPilot;

    @Column(name = "assigned_field_ids", columnDefinition = "TEXT")
    private String assignedFieldIds;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public PilotParticipantEntity() {}

    public PilotParticipantEntity(UUID id, UUID pilotId, UUID userId, String roleInPilot, String assignedFieldIds, Instant joinedAt) {
        this.id = id;
        this.pilotId = pilotId;
        this.userId = userId;
        this.roleInPilot = roleInPilot;
        this.assignedFieldIds = assignedFieldIds;
        this.joinedAt = joinedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPilotId() { return pilotId; }
    public void setPilotId(UUID pilotId) { this.pilotId = pilotId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRoleInPilot() { return roleInPilot; }
    public void setRoleInPilot(String roleInPilot) { this.roleInPilot = roleInPilot; }
    public String getAssignedFieldIds() { return assignedFieldIds; }
    public void setAssignedFieldIds(String assignedFieldIds) { this.assignedFieldIds = assignedFieldIds; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
