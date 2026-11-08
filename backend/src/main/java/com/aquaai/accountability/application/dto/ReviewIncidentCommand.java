package com.aquaai.accountability.application.dto;

import java.util.UUID;

public record ReviewIncidentCommand(
        UUID incidentId,
        boolean accept,
        String notes,
        UUID reviewerId,
        String reviewerName,
        String reviewerRole
) {}
