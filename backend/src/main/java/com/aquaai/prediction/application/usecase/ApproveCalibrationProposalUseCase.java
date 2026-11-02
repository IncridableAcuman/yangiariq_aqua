package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveCalibrationProposalUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final PredictionMethodVersionRepository versionRepository;
    private final GovernanceAuditLogRepository auditLogRepository;

    @Transactional
    public CalibrationProposalDto execute(UUID proposalId, String reviewer, String actorRole, String notes) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Calibration proposal not found: " + proposalId));

        String prevState = proposal.getStatus().name();
        proposal.approve(reviewer, notes);
        CalibrationProposal savedProposal = proposalRepository.save(proposal);

        // Ensure candidate method version is registered in APPROVED state
        PredictionMethodVersion candidateVersion = versionRepository.findByVersionTag(proposal.getCandidateVersionTag())
                .orElseGet(() -> {
                    PredictionMethodVersion newVersion = new PredictionMethodVersion(
                            UUID.randomUUID(),
                            proposal.getPredictionType(),
                            proposal.getCandidateVersionTag(),
                            MethodVersionStatus.APPROVED,
                            "Calibrated candidate version: " + proposal.getCandidateChanges(),
                            proposal.getProposedConfigurationJson(),
                            false,
                            Instant.now(),
                            null,
                            null,
                            reviewer,
                            null,
                            0L
                    );
                    return versionRepository.save(newVersion);
                });

        // Audit Trail
        GovernanceAuditLog auditLog = GovernanceAuditLog.create(
                GovernanceAction.PROPOSAL_APPROVED,
                proposal.getPredictionType(),
                proposal.getCandidateVersionTag(),
                proposal.getId(),
                reviewer,
                actorRole != null ? actorRole : "ADMIN",
                notes != null ? notes : "Proposal approved by administrator after backtest verification.",
                prevState,
                "APPROVED",
                String.format("{\"proposalCode\":\"%s\",\"candidateVersion\":\"%s\"}",
                        proposal.getProposalCode(), proposal.getCandidateVersionTag())
        );
        auditLogRepository.save(auditLog);

        return CalibrationProposalDto.fromDomain(savedProposal);
    }
}
