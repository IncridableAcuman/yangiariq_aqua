package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.BacktestRunDto;
import com.aquaai.prediction.application.dto.CalibrationProposalDetailResponse;
import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.CandidateComparisonDto;
import com.aquaai.prediction.application.dto.GovernanceAuditLogDto;
import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.CandidateComparisonResult;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.ProposalStatus;
import com.aquaai.prediction.domain.port.BacktestRunRepository;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCalibrationProposalDetailUseCase {

    private final CalibrationProposalRepository proposalRepository;
    private final BacktestRunRepository backtestRunRepository;
    private final GovernanceAuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public CalibrationProposalDetailResponse execute(UUID proposalId) {
        CalibrationProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Calibration proposal not found: " + proposalId));

        BacktestRun latestBacktest = backtestRunRepository.findLatestByProposalId(proposalId).orElse(null);
        List<GovernanceAuditLog> auditHistory = auditLogRepository.findByProposalId(proposalId);

        CandidateComparisonDto comparisonDto = null;
        if (latestBacktest != null) {
            boolean readyForApproval = proposal.getStatus() == ProposalStatus.REVIEW_REQUIRED &&
                                       !latestBacktest.isRegressionDetected() &&
                                       latestBacktest.isHasSufficientSample();

            String summary = String.format("Candidate %s yields %s%% MAE improvement over %s across n=%d historical samples.",
                    proposal.getCandidateVersionTag(),
                    latestBacktest.getMaeImprovementPercent(),
                    proposal.getCurrentVersionTag(),
                    latestBacktest.getSampleSize());

            CandidateComparisonResult comp = new CandidateComparisonResult(
                    proposal.getPredictionType(),
                    proposal.getCurrentVersionTag(),
                    proposal.getCandidateVersionTag(),
                    latestBacktest.getSampleSize(),
                    latestBacktest.getCurrentMae(),
                    latestBacktest.getCandidateMae(),
                    latestBacktest.getMaeImprovementPercent(),
                    latestBacktest.getCurrentRmse(),
                    latestBacktest.getCandidateRmse(),
                    latestBacktest.getCurrentMbe(),
                    latestBacktest.getCandidateMbe(),
                    latestBacktest.getCurrentAccuracyPercent(),
                    latestBacktest.getCandidateAccuracyPercent(),
                    latestBacktest.getCurrentF1(),
                    latestBacktest.getCandidateF1(),
                    latestBacktest.isRegressionDetected(),
                    latestBacktest.getRegressionDetails(),
                    summary,
                    readyForApproval
            );
            comparisonDto = CandidateComparisonDto.fromDomain(comp);
        }

        return new CalibrationProposalDetailResponse(
                CalibrationProposalDto.fromDomain(proposal),
                BacktestRunDto.fromDomain(latestBacktest),
                comparisonDto,
                auditHistory.stream().map(GovernanceAuditLogDto::fromDomain).collect(Collectors.toList())
        );
    }
}
