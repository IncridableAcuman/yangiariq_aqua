package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.WaterIncident;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String incidentNumber,
        String title,
        String description,
        IncidentCategory category,
        IncidentSeverity severity,
        EvidenceConfidence confidence,
        IncidentStatus status,
        String sourceModule,
        String sourceEntityType,
        UUID sourceEntityId,
        String findingReference,
        UUID wuaId,
        UUID canalId,
        UUID fieldId,
        String locationDescription,
        Instant detectionTimestamp,
        Instant slaDeadline,
        boolean isOverdue,
        UUID assignedInvestigatorId,
        String assignedInvestigatorName,
        String responsibleOrganization,
        String rootCauseCategory,
        String rootCauseDetails,
        String reviewNotes,
        UUID reviewedBy,
        Instant reviewedAt,
        String resolutionNotes,
        int recurrenceCount,
        UUID parentIncidentId,
        Instant createdAt,
        Instant updatedAt
) {
    public static IncidentResponse from(WaterIncident i, Instant now) {
        if (i == null) return null;
        return new IncidentResponse(
                i.getId(),
                i.getIncidentNumber(),
                i.getTitle(),
                i.getDescription(),
                i.getCategory(),
                i.getSeverity(),
                i.getConfidence(),
                i.getStatus(),
                i.getSourceModule(),
                i.getSourceEntityType(),
                i.getSourceEntityId(),
                i.getFindingReference(),
                i.getWuaId(),
                i.getCanalId(),
                i.getFieldId(),
                i.getLocationDescription(),
                i.getDetectionTimestamp(),
                i.getSlaDeadline(),
                i.isOverdue(now),
                i.getAssignedInvestigatorId(),
                i.getAssignedInvestigatorName(),
                i.getResponsibleOrganization(),
                i.getRootCauseCategory(),
                i.getRootCauseDetails(),
                i.getReviewNotes(),
                i.getReviewedBy(),
                i.getReviewedAt(),
                i.getResolutionNotes(),
                i.getRecurrenceCount(),
                i.getParentIncidentId(),
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }
}
