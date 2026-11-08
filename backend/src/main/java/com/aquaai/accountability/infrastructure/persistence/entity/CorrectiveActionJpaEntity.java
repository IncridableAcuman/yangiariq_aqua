package com.aquaai.accountability.infrastructure.persistence.entity;

import com.aquaai.accountability.domain.model.ActionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "incident_corrective_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectiveActionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "action_number", nullable = false, length = 32)
    private String actionNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "objective", nullable = false, length = 1000)
    private String objective;

    @Column(name = "action_owner_id")
    private UUID actionOwnerId;

    @Column(name = "action_owner_name", length = 255)
    private String actionOwnerName;

    @Column(name = "priority", nullable = false, length = 32)
    private String priority;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "expected_outcome", nullable = false, length = 1000)
    private String expectedOutcome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ActionStatus status;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approval_notes", length = 1000)
    private String approvalNotes;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completion_notes", length = 2000)
    private String completionNotes;

    @Column(name = "verification_requirement", length = 1000)
    private String verificationRequirement;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
