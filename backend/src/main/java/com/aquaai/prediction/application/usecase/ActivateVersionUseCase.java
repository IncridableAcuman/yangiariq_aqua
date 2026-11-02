package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionMethodVersionDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.service.ControlledRolloutManager;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivateVersionUseCase {

    private final PredictionMethodVersionRepository versionRepository;
    private final CalibrationProposalRepository proposalRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final ControlledRolloutManager rolloutManager;

    @Transactional
    public PredictionMethodVersionDto execute(
            UUID versionId,
            String rolloutModeStr,
            String actorId,
            String actorRole,
            String reason
    ) {
        PredictionMethodVersion candidate = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prediction method version not found: " + versionId));

        MethodVersionStatus targetMode = MethodVersionStatus.ACTIVE;
        if (rolloutModeStr != null && !rolloutModeStr.isBlank()) {
            try {
                targetMode = MethodVersionStatus.valueOf(rolloutModeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        // Find associated approved proposal
        CalibrationProposal proposal = proposalRepository.findAll().stream()
                .filter(p -> candidate.getVersionTag().equals(p.getCandidateVersionTag()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot activate version without an associated calibration proposal: " + candidate.getVersionTag()));

        PredictionMethodVersion currentActive = versionRepository.findActiveByPredictionType(candidate.getPredictionType()).orElse(null);

        ControlledRolloutManager.RolloutTransitionResult result = rolloutManager.activateCandidate(
                candidate, currentActive, proposal, targetMode, actorId, actorRole, reason);

        versionRepository.save(result.updatedCandidate());
        if (result.retiredPrevious() != null && result.retiredPrevious() != result.updatedCandidate()) {
            versionRepository.save(result.retiredPrevious());
        }
        proposalRepository.save(result.updatedProposal());
        auditLogRepository.save(result.auditLog());

        return PredictionMethodVersionDto.fromDomain(result.updatedCandidate());
    }
}
