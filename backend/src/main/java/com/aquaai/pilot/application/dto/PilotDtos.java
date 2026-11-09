package com.aquaai.pilot.application.dto;

import com.aquaai.pilot.domain.model.PilotReadinessCheck;
import com.aquaai.pilot.domain.model.ReadinessResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PilotDtos {

    public record CreatePilotRequest(
            String code,
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            String operationalObjectives
    ) {}

    public record UpdatePilotRequest(
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            String operationalObjectives
    ) {}

    public record ApprovePilotRequest(
            String notes
    ) {}

    public record CancelPilotRequest(
            String reason
    ) {}

    public record PilotProgramResponse(
            UUID id,
            String code,
            String name,
            String description,
            String geographicScope,
            LocalDate startDate,
            LocalDate endDate,
            String organization,
            UUID ownerId,
            String operationalObjectives,
            String status,
            UUID approvedBy,
            Instant approvedAt,
            String approvalNotes,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record AddParticipantRequest(
            UUID userId,
            String roleInPilot,
            String assignedFieldIds
    ) {}

    public record PilotParticipantResponse(
            UUID id,
            UUID pilotId,
            UUID userId,
            String roleInPilot,
            String assignedFieldIds,
            Instant joinedAt
    ) {}

    public record AddFieldRequest(
            UUID fieldId,
            String monitoringTarget,
            String measurementPlan,
            String validationCriteria
    ) {}

    public record PilotFieldResponse(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String monitoringTarget,
            String measurementPlan,
            String validationCriteria,
            Instant addedAt
    ) {}

    public record PilotReadinessResponse(
            UUID id,
            UUID pilotId,
            ReadinessResult overallStatus,
            ReadinessResult infrastructureStatus,
            ReadinessResult dataStatus,
            ReadinessResult operationalStatus,
            ReadinessResult governanceStatus,
            List<PilotReadinessCheck> checks,
            UUID assessedBy,
            Instant assessedAt
    ) {}

    public record SubmitGroundTruthRequest(
            UUID fieldId,
            String measurementType,
            Double measuredValue,
            String canonicalUnit,
            Instant measuredAt,
            String collectionMethod,
            String measurementSource,
            String evidenceReference,
            String notes
    ) {}

    public record VerifyGroundTruthRequest(
            String action, // "VERIFY", "REJECT", "REQUEST_REMEASUREMENT"
            String evidenceReference,
            String notes,
            String rejectionReason
    ) {}

    public record GroundTruthResponse(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String measurementType,
            Double measuredValue,
            String canonicalUnit,
            Instant measuredAt,
            Instant submittedAt,
            String collectionMethod,
            String measurementSource,
            UUID submittedBy,
            String verificationStatus,
            UUID verifierId,
            String evidenceReference,
            String notes,
            Instant verifiedAt,
            String rejectionReason
    ) {}

    public record EvaluatePredictionRequest(
            UUID fieldId,
            String predictionId,
            String modelName,
            String modelVersion,
            Instant predictedAt,
            int predictionHorizonHours,
            Double predictedValue,
            Double actualValue,
            UUID groundTruthId
    ) {}

    public record PredictionValidationResponse(
            UUID id,
            UUID pilotId,
            UUID fieldId,
            String predictionId,
            String modelName,
            String modelVersion,
            Instant predictedAt,
            int predictionHorizonHours,
            Double predictedValue,
            Double actualValue,
            UUID groundTruthId,
            Double absoluteError,
            Double relativeErrorPercentage,
            String evaluationStatus,
            Instant evaluatedAt
    ) {}

    public record SubmitFeedbackRequest(
            UUID farmId,
            UUID fieldId,
            UUID sensorId,
            UUID dataSourceId,
            UUID incidentId,
            String category,
            String title,
            String description
    ) {}

    public record UpdateFeedbackStatusRequest(
            String status,
            UUID assignedTo,
            String resolutionNotes
    ) {}

    public record OperatorFeedbackResponse(
            UUID id,
            UUID pilotId,
            UUID farmId,
            UUID fieldId,
            UUID sensorId,
            UUID dataSourceId,
            UUID incidentId,
            String category,
            String title,
            String description,
            String status,
            UUID submittedBy,
            UUID assignedTo,
            String resolutionNotes,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record GenerateEvaluationReportRequest(
            LocalDate periodStart,
            LocalDate periodEnd,
            String limitationsText,
            String recommendationsText
    ) {}

    public record PilotEvaluationReportResponse(
            UUID id,
            UUID pilotId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String dataPerformanceJson,
            String predictionPerformanceJson,
            String operationalPerformanceJson,
            String waterOutcomesJson,
            String limitationsText,
            String recommendationsText,
            UUID createdBy,
            Instant createdAt
    ) {}

    public record PilotOperationalMonitoringResponse(
            UUID pilotId,
            String pilotName,
            String pilotStatus,
            long totalParticipants,
            long totalFields,
            long totalGroundTruthMeasurements,
            long pendingVerifications,
            long verifiedGroundTruths,
            long totalPredictionValidations,
            Double averageAbsoluteError,
            long openFeedbackCount,
            ReadinessResult latestReadinessStatus
    ) {}
}
