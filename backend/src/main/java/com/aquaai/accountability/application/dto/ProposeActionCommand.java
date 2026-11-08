package com.aquaai.accountability.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ProposeActionCommand(
        UUID incidentId,
        String title,
        String description,
        String objective,
        UUID actionOwnerId,
        String actionOwnerName,
        String priority,
        Instant dueDate,
        String expectedOutcome,
        String verificationRequirement,
        UUID proposedBy,
        String proposerName,
        String proposerRole
) {}
