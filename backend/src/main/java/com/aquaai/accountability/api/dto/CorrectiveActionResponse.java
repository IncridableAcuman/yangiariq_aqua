package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.ActionStatus;
import com.aquaai.accountability.domain.model.CorrectiveAction;

import java.time.Instant;
import java.util.UUID;

public record CorrectiveActionResponse(
        UUID id,
        UUID incidentId,
        String actionNumber,
        String title,
        String description,
        String objective,
        UUID actionOwnerId,
        String actionOwnerName,
        String priority,
        Instant dueDate,
        String expectedOutcome,
        ActionStatus status,
        UUID approvedBy,
        Instant approvedAt,
        String approvalNotes,
        UUID completedBy,
        Instant completedAt,
        String completionNotes,
        String verificationRequirement,
        Instant createdAt,
        Instant updatedAt
) {
    public static CorrectiveActionResponse from(CorrectiveAction a) {
        if (a == null) return null;
        return new CorrectiveActionResponse(
                a.getId(),
                a.getIncidentId(),
                a.getActionNumber(),
                a.getTitle(),
                a.getDescription(),
                a.getObjective(),
                a.getActionOwnerId(),
                a.getActionOwnerName(),
                a.getPriority(),
                a.getDueDate(),
                a.getExpectedOutcome(),
                a.getStatus(),
                a.getApprovedBy(),
                a.getApprovedAt(),
                a.getApprovalNotes(),
                a.getCompletedBy(),
                a.getCompletedAt(),
                a.getCompletionNotes(),
                a.getVerificationRequirement(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
