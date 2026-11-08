package com.aquaai.accountability.application.dto;

import java.util.UUID;

public record AssignInvestigatorCommand(
        UUID incidentId,
        UUID investigatorId,
        String investigatorName,
        String organization,
        String reason,
        UUID assignedBy,
        String assignerName,
        String assignerRole
) {}
