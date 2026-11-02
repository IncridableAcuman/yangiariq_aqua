package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.ProposalStatus;

import java.util.Objects;

/**
 * Domain Service governing controlled rollout modes (SHADOW, CANARY, ACTIVE) and ROLLBACK.
 * <p>
 * Ensures zero autonomous self-modification: activation and rollback strictly require
 * authenticated, authorized human administrative decisions.
 */
public class ControlledRolloutManager {

    public record RolloutTransitionResult(
            PredictionMethodVersion updatedCandidate,
            PredictionMethodVersion retiredPrevious,
            CalibrationProposal updatedProposal,
            GovernanceAuditLog auditLog
    ) {}

    public RolloutTransitionResult activateCandidate(
            PredictionMethodVersion candidate,
            PredictionMethodVersion currentActive,
            CalibrationProposal proposal,
            MethodVersionStatus targetRolloutMode,
            String actorId,
            String actorRole,
            String reason
    ) {
        Objects.requireNonNull(candidate, "candidate must not be null");
        Objects.requireNonNull(proposal, "proposal must not be null");

        if (proposal.getStatus() != ProposalStatus.APPROVED) {
            throw new IllegalStateException("Cannot activate a proposal that has not been approved by a human administrator. Current status: " + proposal.getStatus());
        }

        MethodVersionStatus prevStatus = candidate.getStatus();

        if (targetRolloutMode == MethodVersionStatus.SHADOW) {
            candidate.setShadow(actorId);
            GovernanceAuditLog audit = GovernanceAuditLog.create(
                    GovernanceAction.VERSION_SHADOW_STARTED,
                    candidate.getPredictionType(),
                    candidate.getVersionTag(),
                    proposal.getId(),
                    actorId,
                    actorRole,
                    reason != null ? reason : "Candidate launched in SHADOW evaluation mode.",
                    prevStatus.name(),
                    MethodVersionStatus.SHADOW.name(),
                    candidate.getConfigurationJson()
            );
            return new RolloutTransitionResult(candidate, currentActive, proposal, audit);
        }

        if (targetRolloutMode == MethodVersionStatus.CANARY) {
            candidate.setCanary(actorId);
            GovernanceAuditLog audit = GovernanceAuditLog.create(
                    GovernanceAction.VERSION_CANARY_STARTED,
                    candidate.getPredictionType(),
                    candidate.getVersionTag(),
                    proposal.getId(),
                    actorId,
                    actorRole,
                    reason != null ? reason : "Candidate launched in CANARY mode for test parcels.",
                    prevStatus.name(),
                    MethodVersionStatus.CANARY.name(),
                    candidate.getConfigurationJson()
            );
            return new RolloutTransitionResult(candidate, currentActive, proposal, audit);
        }

        // Full Production Activation
        candidate.activate(actorId);
        proposal.markActive();

        if (currentActive != null && currentActive != candidate) {
            currentActive.retire(actorId);
        }

        GovernanceAuditLog audit = GovernanceAuditLog.create(
                GovernanceAction.VERSION_ACTIVATED,
                candidate.getPredictionType(),
                candidate.getVersionTag(),
                proposal.getId(),
                actorId,
                actorRole,
                reason != null ? reason : "Candidate successfully activated in production.",
                prevStatus.name(),
                MethodVersionStatus.ACTIVE.name(),
                candidate.getConfigurationJson()
        );

        return new RolloutTransitionResult(candidate, currentActive, proposal, audit);
    }

    public RolloutTransitionResult executeRollback(
            PredictionMethodVersion currentActive,
            PredictionMethodVersion priorVersion,
            CalibrationProposal activeProposal,
            String actorId,
            String actorRole,
            String rollbackReason
    ) {
        Objects.requireNonNull(currentActive, "currentActive must not be null");
        Objects.requireNonNull(priorVersion, "priorVersion must not be null");

        MethodVersionStatus currentStatus = currentActive.getStatus();
        currentActive.retire(actorId);
        priorVersion.activate(actorId);

        if (activeProposal != null) {
            activeProposal.markRolledBack(rollbackReason);
        }

        GovernanceAuditLog audit = GovernanceAuditLog.create(
                GovernanceAction.VERSION_ROLLBACK,
                currentActive.getPredictionType(),
                currentActive.getVersionTag(),
                activeProposal != null ? activeProposal.getId() : null,
                actorId,
                actorRole,
                rollbackReason != null ? rollbackReason : "Rollback triggered due to operational performance degradation.",
                currentStatus.name(),
                MethodVersionStatus.ACTIVE.name(),
                String.format("{\"rolledBackFrom\":\"%s\",\"restoredTo\":\"%s\"}", currentActive.getVersionTag(), priorVersion.getVersionTag())
        );

        return new RolloutTransitionResult(priorVersion, currentActive, activeProposal, audit);
    }
}
