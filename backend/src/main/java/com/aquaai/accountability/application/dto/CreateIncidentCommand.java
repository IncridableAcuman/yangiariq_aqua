package com.aquaai.accountability.application.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;

import java.time.Instant;
import java.util.UUID;

public record CreateIncidentCommand(
        String title,
        String description,
        IncidentCategory category,
        IncidentSeverity severity,
        EvidenceConfidence confidence,
        String sourceModule,
        String sourceEntityType,
        UUID sourceEntityId,
        String findingReference,
        UUID wuaId,
        UUID canalId,
        UUID fieldId,
        String locationDescription,
        Instant detectionTimestamp,
        UUID reportedBy,
        String reporterName,
        String reporterRole
) {}
