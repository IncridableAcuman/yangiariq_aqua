package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RejectCalibrationProposalUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final GovernanceAuditLogRepository auditLogRepository;

    @Transactional
    public CalibrationProposalDto execute(UUID proposalId, String reviewer, String actorRole, String reason) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Calibration proposal not found: " + proposalId));

        String prevState = proposal.getStatus().name();
        proposal.reject(reviewer, reason);
        CalibrationProposal savedProposal = proposalRepository.save(proposal);

        // Audit Trail
        GovernanceAuditLog auditLog = GovernanceAuditLog.create(
                GovernanceAction.PROPOSAL_REJECTED,
                proposal.getPredictionType(),
                proposal.getCandidateVersionTag(),
                proposal.getId(),
                reviewer,
                actorRole != null ? actorRole : "ADMIN",
                reason != null ? reason : "Proposal rejected by administrator.",
                prevState,
                "REJECTED",
                String.format("{\"proposalCode\":\"%s\",\"reason\":\"%s\"}",
                        proposal.getProposalCode(), reason)
        );
        auditLogRepository.save(auditLog);

        return CalibrationProposalDto.fromDomain(savedProposal);
    }
}
