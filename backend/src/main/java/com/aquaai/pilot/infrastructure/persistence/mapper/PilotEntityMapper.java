package com.aquaai.pilot.infrastructure.persistence.mapper;

import com.aquaai.pilot.domain.model.*;
import com.aquaai.pilot.infrastructure.persistence.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PilotEntityMapper {

    private final ObjectMapper objectMapper;

    public PilotEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PilotProgram toDomain(PilotProgramEntity e) {
        if (e == null) return null;
        return new PilotProgram(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getDescription(),
                e.getGeographicScope(),
                e.getStartDate(),
                e.getEndDate(),
                e.getOrganization(),
                e.getOwnerId(),
                e.getOperationalObjectives(),
                PilotStatus.valueOf(e.getStatus()),
                e.getApprovedBy(),
                e.getApprovedAt(),
                e.getApprovalNotes(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public PilotProgramEntity toEntity(PilotProgram d) {
        if (d == null) return null;
        return new PilotProgramEntity(
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getDescription(),
                d.getGeographicScope(),
                d.getStartDate(),
                d.getEndDate(),
                d.getOrganization(),
                d.getOwnerId(),
                d.getOperationalObjectives(),
                d.getStatus().name(),
                d.getApprovedBy(),
                d.getApprovedAt(),
                d.getApprovalNotes(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }

    public PilotParticipant toDomain(PilotParticipantEntity e) {
        if (e == null) return null;
        return new PilotParticipant(
                e.getId(),
                e.getPilotId(),
                e.getUserId(),
                e.getRoleInPilot(),
                e.getAssignedFieldIds(),
                e.getJoinedAt()
        );
    }

    public PilotParticipantEntity toEntity(PilotParticipant d) {
        if (d == null) return null;
        return new PilotParticipantEntity(
                d.getId(),
                d.getPilotId(),
                d.getUserId(),
                d.getRoleInPilot(),
                d.getAssignedFieldIds(),
                d.getJoinedAt()
        );
    }

    public PilotField toDomain(PilotFieldEntity e) {
        if (e == null) return null;
        return new PilotField(
                e.getId(),
                e.getPilotId(),
                e.getFieldId(),
                e.getMonitoringTarget(),
                e.getMeasurementPlan(),
                e.getValidationCriteria(),
                e.getAddedAt()
        );
    }

    public PilotFieldEntity toEntity(PilotField d) {
        if (d == null) return null;
        return new PilotFieldEntity(
                d.getId(),
                d.getPilotId(),
                d.getFieldId(),
                d.getMonitoringTarget(),
                d.getMeasurementPlan(),
                d.getValidationCriteria(),
                d.getAddedAt()
        );
    }

    public PilotReadinessAssessment toDomain(PilotReadinessEntity e) {
        if (e == null) return null;
        List<PilotReadinessCheck> checks = parseChecks(e.getChecksJson());
        return new PilotReadinessAssessment(
                e.getId(),
                e.getPilotId(),
                ReadinessResult.valueOf(e.getOverallStatus()),
                ReadinessResult.valueOf(e.getInfrastructureStatus()),
                ReadinessResult.valueOf(e.getDataStatus()),
                ReadinessResult.valueOf(e.getOperationalStatus()),
                ReadinessResult.valueOf(e.getGovernanceStatus()),
                checks,
                e.getAssessedBy(),
                e.getAssessedAt()
        );
    }

    public PilotReadinessEntity toEntity(PilotReadinessAssessment d) {
        if (d == null) return null;
        String json = serializeChecks(d.getChecks());
        return new PilotReadinessEntity(
                d.getId(),
                d.getPilotId(),
                d.getOverallStatus().name(),
                d.getInfrastructureStatus().name(),
                d.getDataStatus().name(),
                d.getOperationalStatus().name(),
                d.getGovernanceStatus().name(),
                json,
                d.getAssessedBy(),
                d.getAssessedAt()
        );
    }

    public GroundTruthMeasurement toDomain(GroundTruthValidationEntity e) {
        if (e == null) return null;
        return new GroundTruthMeasurement(
                e.getId(),
                e.getPilotId(),
                e.getFieldId(),
                e.getMeasurementType(),
                e.getMeasuredValue(),
                e.getCanonicalUnit(),
                e.getMeasuredAt(),
                e.getSubmittedAt(),
                e.getCollectionMethod(),
                e.getMeasurementSource(),
                e.getSubmittedBy(),
                GroundTruthStatus.valueOf(e.getVerificationStatus()),
                e.getVerifierId(),
                e.getEvidenceReference(),
                e.getNotes(),
                e.getVerifiedAt(),
                e.getRejectionReason()
        );
    }

    public GroundTruthValidationEntity toEntity(GroundTruthMeasurement d) {
        if (d == null) return null;
        return new GroundTruthValidationEntity(
                d.getId(),
                d.getPilotId(),
                d.getFieldId(),
                d.getMeasurementType(),
                d.getMeasuredValue(),
                d.getCanonicalUnit(),
                d.getMeasuredAt(),
                d.getSubmittedAt(),
                d.getCollectionMethod(),
                d.getMeasurementSource(),
                d.getSubmittedBy(),
                d.getVerificationStatus().name(),
                d.getVerifierId(),
                d.getEvidenceReference(),
                d.getNotes(),
                d.getVerifiedAt(),
                d.getRejectionReason()
        );
    }

    public PredictionValidationRecord toDomain(PredictionValidationEntity e) {
        if (e == null) return null;
        return new PredictionValidationRecord(
                e.getId(),
                e.getPilotId(),
                e.getFieldId(),
                e.getPredictionId(),
                e.getModelName(),
                e.getModelVersion(),
                e.getPredictedAt(),
                e.getPredictionHorizonHours(),
                e.getPredictedValue(),
                e.getActualValue(),
                e.getGroundTruthId(),
                e.getAbsoluteError(),
                e.getRelativeErrorPercentage(),
                PredictionEvaluationStatus.valueOf(e.getEvaluationStatus()),
                e.getEvaluatedAt()
        );
    }

    public PredictionValidationEntity toEntity(PredictionValidationRecord d) {
        if (d == null) return null;
        return new PredictionValidationEntity(
                d.getId(),
                d.getPilotId(),
                d.getFieldId(),
                d.getPredictionId(),
                d.getModelName(),
                d.getModelVersion(),
                d.getPredictedAt(),
                d.getPredictionHorizonHours(),
                d.getPredictedValue(),
                d.getActualValue(),
                d.getGroundTruthId(),
                d.getAbsoluteError(),
                d.getRelativeErrorPercentage(),
                d.getEvaluationStatus().name(),
                d.getEvaluatedAt()
        );
    }

    public OperatorFeedback toDomain(OperatorFeedbackEntity e) {
        if (e == null) return null;
        return new OperatorFeedback(
                e.getId(),
                e.getPilotId(),
                e.getFarmId(),
                e.getFieldId(),
                e.getSensorId(),
                e.getDataSourceId(),
                e.getIncidentId(),
                OperatorFeedbackCategory.valueOf(e.getCategory()),
                e.getTitle(),
                e.getDescription(),
                OperatorFeedbackStatus.valueOf(e.getStatus()),
                e.getSubmittedBy(),
                e.getAssignedTo(),
                e.getResolutionNotes(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public OperatorFeedbackEntity toEntity(OperatorFeedback d) {
        if (d == null) return null;
        return new OperatorFeedbackEntity(
                d.getId(),
                d.getPilotId(),
                d.getFarmId(),
                d.getFieldId(),
                d.getSensorId(),
                d.getDataSourceId(),
                d.getIncidentId(),
                d.getCategory().name(),
                d.getTitle(),
                d.getDescription(),
                d.getStatus().name(),
                d.getSubmittedBy(),
                d.getAssignedTo(),
                d.getResolutionNotes(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }

    public PilotEvaluationSnapshot toDomain(PilotEvaluationSnapshotEntity e) {
        if (e == null) return null;
        return new PilotEvaluationSnapshot(
                e.getId(),
                e.getPilotId(),
                e.getPeriodStart(),
                e.getPeriodEnd(),
                e.getDataPerformanceJson(),
                e.getPredictionPerformanceJson(),
                e.getOperationalPerformanceJson(),
                e.getWaterOutcomesJson(),
                e.getLimitationsText(),
                e.getRecommendationsText(),
                e.getCreatedBy(),
                e.getCreatedAt()
        );
    }

    public PilotEvaluationSnapshotEntity toEntity(PilotEvaluationSnapshot d) {
        if (d == null) return null;
        return new PilotEvaluationSnapshotEntity(
                d.getId(),
                d.getPilotId(),
                d.getPeriodStart(),
                d.getPeriodEnd(),
                d.getDataPerformanceJson(),
                d.getPredictionPerformanceJson(),
                d.getOperationalPerformanceJson(),
                d.getWaterOutcomesJson(),
                d.getLimitationsText(),
                d.getRecommendationsText(),
                d.getCreatedBy(),
                d.getCreatedAt()
        );
    }

    private List<PilotReadinessCheck> parseChecks(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<PilotReadinessCheck>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeChecks(List<PilotReadinessCheck> checks) {
        if (checks == null || checks.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(checks);
        } catch (Exception e) {
            return "[]";
        }
    }
}
