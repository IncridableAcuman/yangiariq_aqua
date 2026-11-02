package com.aquaai.operations.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.operations.api.dto.ScheduleConflictDto;
import com.aquaai.operations.api.dto.WorkflowStepDto;
import com.aquaai.operations.domain.model.OperationalWorkflowState;
import com.aquaai.operations.domain.model.WorkflowStepStatus;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic derivation engine translating cross-module domain entities into
 * a cohesive operational state machine, timeline, and contextual actions.
 */
@Service
public class OperationalWorkflowDerivationService {

    public record DerivationResult(
            OperationalWorkflowState state,
            boolean isBlocked,
            String blockedReason,
            List<WorkflowStepDto> timeline,
            List<String> availableActions
    ) {}

    public DerivationResult derive(
            Anomaly activeAnomaly,
            AgronomicRecommendation activeRecommendation,
            SmartIrrigationPlan activePlan,
            IrrigationEvent activeEvent,
            boolean isHydraulicBlocked,
            String hydraulicBlockedReason,
            ScheduleConflictDto scheduleConflict
    ) {
        Instant now = Instant.now();
        boolean blocked = isHydraulicBlocked || (activePlan != null && activePlan.isBlocked());
        String blockReason = hydraulicBlockedReason != null ? hydraulicBlockedReason :
                (activePlan != null ? activePlan.getBlockedReason() : null);

        // 1. Determine Current Operational Workflow State
        OperationalWorkflowState currentState;
        if (blocked) {
            currentState = OperationalWorkflowState.BLOCKED;
        } else if (activePlan != null) {
            IrrigationPlanStatus planStatus = activePlan.getStatus();
            switch (planStatus) {
                case CANCELLED -> currentState = OperationalWorkflowState.CANCELLED;
                case EXPIRED -> currentState = OperationalWorkflowState.EXPIRED;
                case COMPLETED -> {
                    boolean eventCompleted = activeEvent != null && activeEvent.getStatus() == IrrigationEventStatus.COMPLETED;
                    currentState = eventCompleted ? OperationalWorkflowState.VERIFIED : OperationalWorkflowState.COMPLETED;
                }
                case IN_PROGRESS -> currentState = OperationalWorkflowState.IN_PROGRESS;
                case SCHEDULED -> {
                    Instant plannedStart = activePlan.getPlannedStartAt();
                    boolean isReady = plannedStart != null && !now.isBefore(plannedStart);
                    currentState = isReady ? OperationalWorkflowState.READY : OperationalWorkflowState.SCHEDULED;
                }
                case APPROVED -> currentState = OperationalWorkflowState.APPROVED;
                case PROPOSED -> currentState = OperationalWorkflowState.APPROVAL_REQUIRED;
                case DRAFT -> currentState = OperationalWorkflowState.PLAN_PROPOSED;
                default -> currentState = OperationalWorkflowState.PLAN_REQUIRED;
            }
        } else if (activeRecommendation != null) {
            RecommendationStatus recStatus = activeRecommendation.getStatus();
            if (recStatus == RecommendationStatus.GENERATED) {
                currentState = OperationalWorkflowState.RECOMMENDATION_READY;
            } else if (recStatus == RecommendationStatus.VIEWED || recStatus == RecommendationStatus.ACCEPTED) {
                currentState = OperationalWorkflowState.PLAN_REQUIRED;
            } else {
                currentState = OperationalWorkflowState.REVIEW_REQUIRED;
            }
        } else if (activeAnomaly != null) {
            currentState = OperationalWorkflowState.DETECTED;
        } else {
            currentState = OperationalWorkflowState.COMPLETED; // Normal / idle
        }

        // 2. Build Operational Timeline Milestones
        List<WorkflowStepDto> timeline = buildTimeline(
                currentState,
                activeAnomaly,
                activeRecommendation,
                activePlan,
                activeEvent,
                blocked,
                blockReason
        );

        // 3. Derive Contextual Actions
        List<String> availableActions = deriveActions(currentState, blocked, scheduleConflict);

        return new DerivationResult(
                currentState,
                blocked,
                blockReason,
                timeline,
                availableActions
        );
    }

    private List<WorkflowStepDto> buildTimeline(
            OperationalWorkflowState currentState,
            Anomaly anomaly,
            AgronomicRecommendation recommendation,
            SmartIrrigationPlan plan,
            IrrigationEvent event,
            boolean isBlocked,
            String blockReason
    ) {
        List<WorkflowStepDto> steps = new ArrayList<>();

        // Step 1: Issue / Anomaly / Soil Deficit Detected
        boolean hasIssue = anomaly != null || recommendation != null || plan != null;
        steps.add(WorkflowStepDto.builder()
                .stepKey("DETECTED")
                .title("Problem / Need Detected")
                .status(hasIssue ? WorkflowStepStatus.COMPLETED : WorkflowStepStatus.CURRENT)
                .timestamp(anomaly != null ? anomaly.getDetectedAt() : (recommendation != null ? recommendation.getGeneratedAt() : null))
                .actor(anomaly != null ? "TELEMETRY_ANOMALY_ENGINE" : "AGRONOMIC_ASSESSMENT")
                .reason(anomaly != null ? anomaly.getDescription() : "Soil moisture deficit or scheduled turn required")
                .availableAction(null)
                .build());

        // Step 2: Agronomic Recommendation Generated
        boolean recCompleted = recommendation != null && recommendation.getStatus() != RecommendationStatus.GENERATED;
        boolean recCurrent = currentState == OperationalWorkflowState.RECOMMENDATION_READY;
        steps.add(WorkflowStepDto.builder()
                .stepKey("RECOMMENDATION_READY")
                .title("Agronomic Recommendation")
                .status(recCompleted || plan != null ? WorkflowStepStatus.COMPLETED : (recCurrent ? WorkflowStepStatus.CURRENT : WorkflowStepStatus.UPCOMING))
                .timestamp(recommendation != null ? recommendation.getGeneratedAt() : null)
                .actor("AI_AGRONOMIC_ADVISOR")
                .reason(recommendation != null ? recommendation.getSummary() : "Awaiting assessment triggers")
                .availableAction(recCurrent ? "PREPARE_PLAN" : null)
                .build());

        // Step 3: Smart Irrigation Plan Prepared
        boolean planExists = plan != null;
        boolean planDraftOrProposed = planExists && (plan.getStatus() == IrrigationPlanStatus.DRAFT || plan.getStatus() == IrrigationPlanStatus.PROPOSED);
        steps.add(WorkflowStepDto.builder()
                .stepKey("PLAN_PROPOSED")
                .title("Plan Prepared & Constraints Checked")
                .status(planExists && !planDraftOrProposed ? WorkflowStepStatus.COMPLETED :
                        (planDraftOrProposed ? WorkflowStepStatus.CURRENT :
                                (currentState == OperationalWorkflowState.PLAN_REQUIRED ? WorkflowStepStatus.CURRENT : WorkflowStepStatus.UPCOMING)))
                .timestamp(plan != null ? plan.getCreatedAt() : null)
                .actor(plan != null ? plan.getSource() : null)
                .reason(plan != null ? plan.getReason() : "Awaiting plan generation from recommendation")
                .availableAction(currentState == OperationalWorkflowState.PLAN_REQUIRED ? "PREPARE_PLAN" : null)
                .build());

        // Step 4: Human Review & Approval
        boolean planApproved = plan != null && (plan.getStatus() == IrrigationPlanStatus.APPROVED
                || plan.getStatus() == IrrigationPlanStatus.SCHEDULED
                || plan.getStatus() == IrrigationPlanStatus.IN_PROGRESS
                || plan.getStatus() == IrrigationPlanStatus.COMPLETED);
        boolean approvalPending = currentState == OperationalWorkflowState.APPROVAL_REQUIRED || currentState == OperationalWorkflowState.PLAN_PROPOSED;

        steps.add(WorkflowStepDto.builder()
                .stepKey("APPROVED")
                .title("Human Operational Approval")
                .status(isBlocked ? WorkflowStepStatus.BLOCKED :
                        (planApproved ? WorkflowStepStatus.COMPLETED :
                                (approvalPending ? WorkflowStepStatus.CURRENT : WorkflowStepStatus.UPCOMING)))
                .timestamp(plan != null ? plan.getApprovedAt() : null)
                .actor(plan != null && plan.getApprovedBy() != null ? "OPERATOR / FARMER (" + plan.getApprovedBy() + ")" : "PENDING_HUMAN_REVIEW")
                .reason(isBlocked ? blockReason : (planApproved ? "Mandatory operational constraints acknowledged and verified." : "Human approval required prior to dispatch."))
                .availableAction(approvalPending && !isBlocked ? "APPROVE_PLAN" : null)
                .build());

        // Step 5: Irrigation Schedule Dispatched
        boolean isScheduled = plan != null && (plan.getStatus() == IrrigationPlanStatus.SCHEDULED
                || plan.getStatus() == IrrigationPlanStatus.IN_PROGRESS
                || plan.getStatus() == IrrigationPlanStatus.COMPLETED);
        boolean schedulePending = currentState == OperationalWorkflowState.APPROVED;

        steps.add(WorkflowStepDto.builder()
                .stepKey("SCHEDULED")
                .title("Irrigation Turn Scheduled")
                .status(isScheduled ? WorkflowStepStatus.COMPLETED :
                        (schedulePending ? WorkflowStepStatus.CURRENT : WorkflowStepStatus.UPCOMING))
                .timestamp(plan != null ? plan.getPlannedStartAt() : null)
                .actor("SCHEDULING_ENGINE")
                .reason(plan != null ? "Scheduled delivery window: " + plan.getPlannedStartAt() + " to " + plan.getPlannedEndAt() : "Awaiting approval")
                .availableAction(schedulePending ? "SCHEDULE_PLAN" : null)
                .build());

        // Step 6: Operation Execution
        boolean isStarted = plan != null && (plan.getStatus() == IrrigationPlanStatus.IN_PROGRESS
                || plan.getStatus() == IrrigationPlanStatus.COMPLETED);
        boolean readyToStart = currentState == OperationalWorkflowState.READY || currentState == OperationalWorkflowState.SCHEDULED;

        steps.add(WorkflowStepDto.builder()
                .stepKey("IN_PROGRESS")
                .title("Field Operation Recorded")
                .status(plan != null && plan.getStatus() == IrrigationPlanStatus.COMPLETED ? WorkflowStepStatus.COMPLETED :
                        (currentState == OperationalWorkflowState.IN_PROGRESS ? WorkflowStepStatus.CURRENT :
                                (readyToStart ? WorkflowStepStatus.UPCOMING : WorkflowStepStatus.UPCOMING)))
                .timestamp(event != null ? event.getStartedAt() : (plan != null ? plan.getPlannedStartAt() : null))
                .actor(event != null ? "FIELD_OPERATOR" : "MANUAL_RECORDING")
                .reason(currentState == OperationalWorkflowState.IN_PROGRESS ? "Irrigation currently in progress in the field." :
                        (plan != null && plan.getStatus() == IrrigationPlanStatus.COMPLETED ? "Field irrigation completed and recorded." : "Ready for manual dispatch."))
                .availableAction(readyToStart ? "START_OPERATION" : (currentState == OperationalWorkflowState.IN_PROGRESS ? "COMPLETE_OPERATION" : null))
                .build());

        // Step 7: Post-Irrigation Verification
        boolean isVerified = currentState == OperationalWorkflowState.VERIFIED;
        boolean canVerify = currentState == OperationalWorkflowState.COMPLETED || isVerified;

        steps.add(WorkflowStepDto.builder()
                .stepKey("VERIFIED")
                .title("Result Verification & Telemetry Audit")
                .status(isVerified ? WorkflowStepStatus.COMPLETED :
                        (canVerify ? WorkflowStepStatus.CURRENT : WorkflowStepStatus.UPCOMING))
                .timestamp(event != null && event.getEndedAt() != null ? event.getEndedAt() : (plan != null ? plan.getUpdatedAt() : null))
                .actor("INTELLIGENCE_AUDITOR")
                .reason(canVerify ? "Planned vs. actual volumetric comparison and telemetry delta calculated." : "Awaiting completion.")
                .availableAction(canVerify ? "VERIFY_RESULT" : null)
                .build());

        return Collections.unmodifiableList(steps);
    }

    private List<String> deriveActions(OperationalWorkflowState state, boolean isBlocked, ScheduleConflictDto conflict) {
        if (isBlocked) {
            return List.of("INVESTIGATE_SAFETY");
        }

        List<String> actions = new ArrayList<>();
        switch (state) {
            case DETECTED, REVIEW_REQUIRED -> {
                actions.add("VIEW_TELEMETRY");
                actions.add("EVALUATE_RECOMMENDATION");
            }
            case RECOMMENDATION_READY -> {
                actions.add("PREPARE_PLAN");
                actions.add("DISMISS_RECOMMENDATION");
            }
            case PLAN_REQUIRED -> actions.add("PREPARE_PLAN");
            case PLAN_PROPOSED, APPROVAL_REQUIRED -> {
                actions.add("APPROVE_PLAN");
                actions.add("REJECT_PLAN");
                actions.add("EDIT_PLAN");
            }
            case APPROVED -> {
                actions.add("SCHEDULE_PLAN");
                actions.add("START_OPERATION");
                actions.add("CANCEL_PLAN");
            }
            case SCHEDULED, READY -> {
                actions.add("START_OPERATION");
                actions.add("SCHEDULE_PLAN");
                actions.add("CANCEL_PLAN");
            }
            case IN_PROGRESS -> {
                actions.add("COMPLETE_OPERATION");
                actions.add("CANCEL_PLAN");
            }
            case COMPLETED, VERIFIED -> {
                actions.add("VERIFY_RESULT");
                actions.add("NEW_CYCLE");
            }
            case REJECTED, CANCELLED, EXPIRED -> actions.add("PREPARE_PLAN");
        }

        return Collections.unmodifiableList(actions);
    }
}
