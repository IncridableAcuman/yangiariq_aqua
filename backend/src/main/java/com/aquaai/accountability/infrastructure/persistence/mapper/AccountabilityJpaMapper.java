package com.aquaai.accountability.infrastructure.persistence.mapper;

import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.infrastructure.persistence.entity.CorrectiveActionJpaEntity;
import com.aquaai.accountability.infrastructure.persistence.entity.IncidentEvidenceJpaEntity;
import com.aquaai.accountability.infrastructure.persistence.entity.IncidentTimelineJpaEntity;
import com.aquaai.accountability.infrastructure.persistence.entity.IncidentVerificationJpaEntity;
import com.aquaai.accountability.infrastructure.persistence.entity.WaterIncidentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountabilityJpaMapper {

    public WaterIncidentJpaEntity toEntity(WaterIncident domain) {
        if (domain == null) return null;
        return WaterIncidentJpaEntity.builder()
                .id(domain.getId())
                .incidentNumber(domain.getIncidentNumber())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .severity(domain.getSeverity())
                .confidence(domain.getConfidence())
                .status(domain.getStatus())
                .sourceModule(domain.getSourceModule())
                .sourceEntityType(domain.getSourceEntityType())
                .sourceEntityId(domain.getSourceEntityId())
                .findingReference(domain.getFindingReference())
                .wuaId(domain.getWuaId())
                .canalId(domain.getCanalId())
                .fieldId(domain.getFieldId())
                .locationDescription(domain.getLocationDescription())
                .detectionTimestamp(domain.getDetectionTimestamp())
                .slaDeadline(domain.getSlaDeadline())
                .assignedInvestigatorId(domain.getAssignedInvestigatorId())
                .assignedInvestigatorName(domain.getAssignedInvestigatorName())
                .responsibleOrganization(domain.getResponsibleOrganization())
                .rootCauseCategory(domain.getRootCauseCategory())
                .rootCauseDetails(domain.getRootCauseDetails())
                .reviewNotes(domain.getReviewNotes())
                .reviewedBy(domain.getReviewedBy())
                .reviewedAt(domain.getReviewedAt())
                .resolutionNotes(domain.getResolutionNotes())
                .recurrenceCount(domain.getRecurrenceCount())
                .parentIncidentId(domain.getParentIncidentId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public WaterIncident toDomain(WaterIncidentJpaEntity entity) {
        if (entity == null) return null;
        return WaterIncident.builder()
                .id(entity.getId())
                .incidentNumber(entity.getIncidentNumber())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .severity(entity.getSeverity())
                .confidence(entity.getConfidence())
                .status(entity.getStatus())
                .sourceModule(entity.getSourceModule())
                .sourceEntityType(entity.getSourceEntityType())
                .sourceEntityId(entity.getSourceEntityId())
                .findingReference(entity.getFindingReference())
                .wuaId(entity.getWuaId())
                .canalId(entity.getCanalId())
                .fieldId(entity.getFieldId())
                .locationDescription(entity.getLocationDescription())
                .detectionTimestamp(entity.getDetectionTimestamp())
                .slaDeadline(entity.getSlaDeadline())
                .assignedInvestigatorId(entity.getAssignedInvestigatorId())
                .assignedInvestigatorName(entity.getAssignedInvestigatorName())
                .responsibleOrganization(entity.getResponsibleOrganization())
                .rootCauseCategory(entity.getRootCauseCategory())
                .rootCauseDetails(entity.getRootCauseDetails())
                .reviewNotes(entity.getReviewNotes())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .resolutionNotes(entity.getResolutionNotes())
                .recurrenceCount(entity.getRecurrenceCount())
                .parentIncidentId(entity.getParentIncidentId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public IncidentEvidenceJpaEntity toEntity(IncidentEvidence domain) {
        if (domain == null) return null;
        return IncidentEvidenceJpaEntity.builder()
                .id(domain.getId())
                .incidentId(domain.getIncidentId())
                .evidenceType(domain.getEvidenceType())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .evidenceUri(domain.getEvidenceUri())
                .confidence(domain.getConfidence())
                .isSimulated(domain.isSimulated())
                .sourceReference(domain.getSourceReference())
                .recordedBy(domain.getRecordedBy())
                .recordedAt(domain.getRecordedAt())
                .build();
    }

    public IncidentEvidence toDomain(IncidentEvidenceJpaEntity entity) {
        if (entity == null) return null;
        return IncidentEvidence.builder()
                .id(entity.getId())
                .incidentId(entity.getIncidentId())
                .evidenceType(entity.getEvidenceType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .evidenceUri(entity.getEvidenceUri())
                .confidence(entity.getConfidence())
                .isSimulated(entity.isSimulated())
                .sourceReference(entity.getSourceReference())
                .recordedBy(entity.getRecordedBy())
                .recordedAt(entity.getRecordedAt())
                .build();
    }

    public CorrectiveActionJpaEntity toEntity(CorrectiveAction domain) {
        if (domain == null) return null;
        return CorrectiveActionJpaEntity.builder()
                .id(domain.getId())
                .incidentId(domain.getIncidentId())
                .actionNumber(domain.getActionNumber())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .objective(domain.getObjective())
                .actionOwnerId(domain.getActionOwnerId())
                .actionOwnerName(domain.getActionOwnerName())
                .priority(domain.getPriority())
                .dueDate(domain.getDueDate())
                .expectedOutcome(domain.getExpectedOutcome())
                .status(domain.getStatus())
                .approvedBy(domain.getApprovedBy())
                .approvedAt(domain.getApprovedAt())
                .approvalNotes(domain.getApprovalNotes())
                .completedBy(domain.getCompletedBy())
                .completedAt(domain.getCompletedAt())
                .completionNotes(domain.getCompletionNotes())
                .verificationRequirement(domain.getVerificationRequirement())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public CorrectiveAction toDomain(CorrectiveActionJpaEntity entity) {
        if (entity == null) return null;
        return CorrectiveAction.builder()
                .id(entity.getId())
                .incidentId(entity.getIncidentId())
                .actionNumber(entity.getActionNumber())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .objective(entity.getObjective())
                .actionOwnerId(entity.getActionOwnerId())
                .actionOwnerName(entity.getActionOwnerName())
                .priority(entity.getPriority())
                .dueDate(entity.getDueDate())
                .expectedOutcome(entity.getExpectedOutcome())
                .status(entity.getStatus())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .approvalNotes(entity.getApprovalNotes())
                .completedBy(entity.getCompletedBy())
                .completedAt(entity.getCompletedAt())
                .completionNotes(entity.getCompletionNotes())
                .verificationRequirement(entity.getVerificationRequirement())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public IncidentVerificationJpaEntity toEntity(IncidentVerification domain) {
        if (domain == null) return null;
        return IncidentVerificationJpaEntity.builder()
                .id(domain.getId())
                .incidentId(domain.getIncidentId())
                .actionId(domain.getActionId())
                .verifierId(domain.getVerifierId())
                .verifierName(domain.getVerifierName())
                .verifiedAt(domain.getVerifiedAt())
                .verificationMethod(domain.getVerificationMethod())
                .result(domain.getResult())
                .explanatoryNotes(domain.getExplanatoryNotes())
                .supportingEvidenceUri(domain.getSupportingEvidenceUri())
                .build();
    }

    public IncidentVerification toDomain(IncidentVerificationJpaEntity entity) {
        if (entity == null) return null;
        return IncidentVerification.builder()
                .id(entity.getId())
                .incidentId(entity.getIncidentId())
                .actionId(entity.getActionId())
                .verifierId(entity.getVerifierId())
                .verifierName(entity.getVerifierName())
                .verifiedAt(entity.getVerifiedAt())
                .verificationMethod(entity.getVerificationMethod())
                .result(entity.getResult())
                .explanatoryNotes(entity.getExplanatoryNotes())
                .supportingEvidenceUri(entity.getSupportingEvidenceUri())
                .build();
    }

    public IncidentTimelineJpaEntity toEntity(IncidentTimelineEntry domain) {
        if (domain == null) return null;
        return IncidentTimelineJpaEntity.builder()
                .id(domain.getId())
                .incidentId(domain.getIncidentId())
                .eventType(domain.getEventType())
                .actorId(domain.getActorId())
                .actorName(domain.getActorName())
                .actorRole(domain.getActorRole())
                .timestamp(domain.getTimestamp())
                .previousState(domain.getPreviousState())
                .newState(domain.getNewState())
                .reason(domain.getReason())
                .correlationId(domain.getCorrelationId())
                .metadataJson(domain.getMetadataJson())
                .build();
    }

    public IncidentTimelineEntry toDomain(IncidentTimelineJpaEntity entity) {
        if (entity == null) return null;
        return IncidentTimelineEntry.builder()
                .id(entity.getId())
                .incidentId(entity.getIncidentId())
                .eventType(entity.getEventType())
                .actorId(entity.getActorId())
                .actorName(entity.getActorName())
                .actorRole(entity.getActorRole())
                .timestamp(entity.getTimestamp())
                .previousState(entity.getPreviousState())
                .newState(entity.getNewState())
                .reason(entity.getReason())
                .correlationId(entity.getCorrelationId())
                .metadataJson(entity.getMetadataJson())
                .build();
    }
}
