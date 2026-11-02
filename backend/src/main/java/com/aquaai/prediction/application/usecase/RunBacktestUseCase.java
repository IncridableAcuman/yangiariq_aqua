package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.BacktestRunDto;
import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.port.BacktestRunRepository;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.service.BacktestingEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunBacktestUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final PredictionEvaluationRepository evaluationRepository;
    private final BacktestRunRepository backtestRunRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final BacktestingEngine backtestingEngine;

    @Transactional
    public BacktestRunDto execute(UUID proposalId, String executedBy, String actorRole) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Calibration proposal not found: " + proposalId));

        proposal.startBacktest();
        proposalRepository.save(proposal);

        Instant to = Instant.now();
        Instant from = to.minus(30, ChronoUnit.DAYS);

        // Fetch authoritative historical evaluations
        List<PredictionEvaluation> historicalEvaluations = evaluationRepository.findByPredictionTypeAndPeriod(
                proposal.getPredictionType(), from, to);

        BacktestRun backtestRun = backtestingEngine.executeBacktest(
                proposal.getId(),
                proposal.getCurrentVersionTag(),
                proposal.getCandidateVersionTag(),
                proposal.getPredictionType(),
                proposal.getProposedConfigurationJson(),
                historicalEvaluations,
                from,
                to,
                executedBy != null ? executedBy : "SYSTEM_ADMIN"
        );

        BacktestRun savedRun = backtestRunRepository.save(backtestRun);

        proposal.completeBacktest();
        proposalRepository.save(proposal);

        // Audit Trail
        GovernanceAuditLog auditLog = GovernanceAuditLog.create(
                GovernanceAction.BACKTEST_EXECUTED,
                proposal.getPredictionType(),
                proposal.getCandidateVersionTag(),
                proposal.getId(),
                executedBy != null ? executedBy : "ADMIN",
                actorRole != null ? actorRole : "ADMIN",
                String.format("Backtest executed across n=%d samples. MAE Improvement: %s%%. Regression: %s.",
                        savedRun.getSampleSize(), savedRun.getMaeImprovementPercent(), savedRun.isRegressionDetected()),
                "DRAFT",
                "REVIEW_REQUIRED",
                String.format("{\"improvementPercent\":%s,\"sampleSize\":%d,\"regression\":%s}",
                        savedRun.getMaeImprovementPercent(), savedRun.getSampleSize(), savedRun.isRegressionDetected())
        );
        auditLogRepository.save(auditLog);

        return BacktestRunDto.fromDomain(savedRun);
    }
}
