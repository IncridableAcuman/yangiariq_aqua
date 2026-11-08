package com.aquaai.accountability.application.dto;

import java.util.UUID;

public record CompleteActionCommand(
        UUID incidentId,
        UUID actionId,
        String completionNotes,
        UUID completedBy,
        String completerName,
        String completerRole
) {}
