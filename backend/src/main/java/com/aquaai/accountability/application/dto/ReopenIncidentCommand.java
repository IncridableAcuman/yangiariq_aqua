package com.aquaai.accountability.application.dto;

import java.util.UUID;

public record ReopenIncidentCommand(
        UUID incidentId,
        String reason,
        UUID reopenedBy,
        String reopenerName,
        String reopenerRole
) {}
