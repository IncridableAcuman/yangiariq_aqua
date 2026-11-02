package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionMethodVersionDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.service.ControlledRolloutManager;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RollbackVersionUseCase {

    private final PredictionMethodVersionRepository versionRepository;
    private final CalibrationProposalRepository proposalRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final ControlledRolloutManager rolloutManager;

    @Transactional
    public PredictionMethodVersionDto execute(
            PredictionType type,
            String priorVersionTag,
            String actorId,
            String actorRole,
            String reason
    ) {
        PredictionMethodVersion currentActive = versionRepository.findActiveByPredictionType(type)
                .orElseThrow(() -> new ResourceNotFoundException("No active version found for prediction type: " + type));

        PredictionMethodVersion priorVersion = versionRepository.findByVersionTag(priorVersionTag)
                .orElseThrow(() -> new ResourceNotFoundException("Prior prediction method version not found: " + priorVersionTag));

        CalibrationProposal activeProposal = proposalRepository.findAll().stream()
                .filter(p -> currentActive.getVersionTag().equals(p.getCandidateVersionTag()))
                .findFirst()
                .orElse(null);

        ControlledRolloutManager.RolloutTransitionResult result = rolloutManager.executeRollback(
                currentActive, priorVersion, activeProposal, actorId, actorRole, reason);

        versionRepository.save(result.retiredPrevious()); // retired current
        versionRepository.save(result.updatedCandidate()); // reactivated prior
        if (result.updatedProposal() != null) {
            proposalRepository.save(result.updatedProposal());
        }
        auditLogRepository.save(result.auditLog());

        return PredictionMethodVersionDto.fromDomain(result.updatedCandidate());
    }
}
