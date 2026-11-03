package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.CreateCandidateProposalRequest;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCandidateProposalUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final PredictionMethodVersionRepository versionRepository;
    private final GovernanceAuditLogRepository auditLogRepository;

    @Transactional
    public CalibrationProposalDto execute(
            CreateCandidateProposalRequest request,
            String actorId,
            String actorRole
    ) {
        Objects.requireNonNull(request, "request must not be null");
        PredictionType predictionType;
        try {
            predictionType = PredictionType.valueOf(request.predictionType().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid prediction type: " + request.predictionType());
        }

        // Enforce hard hydraulic safety constraints
        PredictionMethodVersion.validateSafetyParameters(request.proposedConfigurationJson());

        // Find active production version for this prediction type
        PredictionMethodVersion activeVersion = versionRepository.findActiveByPredictionType(predictionType)
                .orElseThrow(() -> new ResourceNotFoundException("No active version found for prediction type: " + predictionType));

        String nextCandidateTag = activeVersion.getVersionTag().endsWith("_V1")
                ? activeVersion.getVersionTag().replace("_V1", "_V2")
                : activeVersion.getVersionTag() + "_CANDIDATE";

        GovernanceRiskLevel riskLevel = GovernanceRiskLevel.MEDIUM;
        if (request.riskLevel() != null && !request.riskLevel().isBlank()) {
            try {
                riskLevel = GovernanceRiskLevel.valueOf(request.riskLevel().toUpperCase());
            } catch (Exception ignored) {}
        }

        String proposalCode = "PROP-" + System.currentTimeMillis() % 100000;
        CalibrationProposal proposal = CalibrationProposal.create(
                proposalCode,
                predictionType,
                activeVersion.getVersionTag(),
                nextCandidateTag,
                request.reason(),
                "Human operator calibration proposal with backtest review requirement.",
                request.candidateChanges(),
                request.proposedConfigurationJson(),
                request.expectedEffect(),
                riskLevel,
                "ALL_PARCELS",
                actorId != null ? actorId : "ADMIN"
        );

        CalibrationProposal saved = proposalRepository.save(proposal);

        // Register candidate method version in CANDIDATE status
        versionRepository.findByVersionTag(nextCandidateTag).orElseGet(() -> {
            PredictionMethodVersion newCand = PredictionMethodVersion.createCandidate(
                    predictionType,
                    nextCandidateTag,
                    "Candidate version: " + request.candidateChanges(),
                    request.proposedConfigurationJson(),
                    actorId
            );
            return versionRepository.save(newCand);
        });

        // Audit log
        GovernanceAuditLog auditLog = GovernanceAuditLog.create(
                GovernanceAction.PROPOSAL_CREATED,
                predictionType,
                nextCandidateTag,
                saved.getId(),
                actorId,
                actorRole != null ? actorRole : "ADMIN",
                request.reason(),
                "NONE",
                "DRAFT",
                request.proposedConfigurationJson()
        );
        auditLogRepository.save(auditLog);

        return CalibrationProposalDto.fromDomain(saved);
    }
}
