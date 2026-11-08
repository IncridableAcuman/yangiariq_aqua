package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull IncidentCategory category,
        @NotNull IncidentSeverity severity,
        EvidenceConfidence confidence,
        String sourceModule,
        String sourceEntityType,
        UUID sourceEntityId,
        String findingReference,
        UUID wuaId,
        UUID canalId,
        UUID fieldId,
        String locationDescription,
        Instant detectionTimestamp
) {}
