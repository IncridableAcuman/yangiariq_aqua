package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "governance_audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAuditLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "version_tag", length = 64)
    private String versionTag;

    @Column(name = "proposal_id")
    private UUID proposalId;

    @Column(name = "actor_id", nullable = false, length = 64)
    private String actorId;

    @Column(name = "actor_role", nullable = false, length = 32)
    private String actorRole;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "previous_state", length = 64)
    private String previousState;

    @Column(name = "new_state", length = 64)
    private String newState;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;
}
