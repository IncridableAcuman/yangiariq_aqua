package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.PredictionEvaluationOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionTypePerformanceDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.service.CalibrationProposalGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateCalibrationProposalUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final PredictionMethodVersionRepository versionRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final CalibrationProposalGenerator proposalGenerator;
    private final GetPredictionEvaluationOverviewUseCase evaluationOverviewUseCase;

    @Transactional
    public Optional<CalibrationProposalDto> execute(PredictionType type, String actorId, String actorRole) {
        PredictionMethodVersion currentVersion = versionRepository.findActiveByPredictionType(type).orElse(null);
        if (currentVersion == null) {
            return Optional.empty();
        }

        PredictionEvaluationOverviewResponse overview = evaluationOverviewUseCase.execute(null, null);
        if (overview == null || overview.performanceByType() == null) {
            return Optional.empty();
        }

        PredictionTypePerformanceDto perf = overview.performanceByType().stream()
                .filter(p -> type == p.predictionType())
                .findFirst()
                .orElse(null);

        if (perf == null || perf.numericalMetrics() == null) {
            return Optional.empty();
        }

        boolean driftDetected = overview.driftSummary() != null && overview.driftSummary().driftDetected();
        String driftSummary = overview.driftSummary() != null ? overview.driftSummary().explanation() : null;

        Optional<CalibrationProposal> proposalOpt = proposalGenerator.generateProposalIfDegraded(
                type,
                currentVersion,
                perf.numericalMetrics(),
                driftDetected,
                driftSummary
        );

        if (proposalOpt.isEmpty()) {
            return Optional.empty();
        }

        CalibrationProposal proposal = proposalOpt.get();
        CalibrationProposal savedProposal = proposalRepository.save(proposal);

        // Register candidate method version in DRAFT / CANDIDATE state
        versionRepository.findByVersionTag(proposal.getCandidateVersionTag()).orElseGet(() -> {
            PredictionMethodVersion candidate = new PredictionMethodVersion(
                    UUID.randomUUID(),
                    proposal.getPredictionType(),
                    proposal.getCandidateVersionTag(),
                    MethodVersionStatus.CANDIDATE,
                    "Automated candidate proposal: " + proposal.getCandidateChanges(),
                    proposal.getProposedConfigurationJson(),
                    false,
                    Instant.now(),
                    null,
                    null,
                    actorId != null ? actorId : "SYSTEM",
                    null,
                    0L
            );
            return versionRepository.save(candidate);
        });

        // Audit Trail
        GovernanceAuditLog auditLog = GovernanceAuditLog.create(
                GovernanceAction.PROPOSAL_CREATED,
                proposal.getPredictionType(),
                proposal.getCandidateVersionTag(),
                proposal.getId(),
                actorId != null ? actorId : "SYSTEM",
                actorRole != null ? actorRole : "SYSTEM",
                "Automated calibration proposal generated due to detected model degradation.",
                null,
                "DRAFT",
                String.format("{\"proposalCode\":\"%s\",\"reason\":\"%s\"}", proposal.getProposalCode(), proposal.getReason())
        );
        auditLogRepository.save(auditLog);

        return Optional.of(CalibrationProposalDto.fromDomain(savedProposal));
    }
}
