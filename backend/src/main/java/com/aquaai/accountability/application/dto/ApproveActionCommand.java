package com.aquaai.accountability.application.dto;

import java.util.UUID;

public record ApproveActionCommand(
        UUID incidentId,
        UUID actionId,
        String notes,
        UUID approverId,
        String approverName,
        String approverRole
) {}
