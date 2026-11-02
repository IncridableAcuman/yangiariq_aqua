package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.GovernanceAuditLogDto;
import com.aquaai.prediction.application.dto.GovernanceOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionEvaluationOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionMethodVersionDto;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetGovernanceOverviewUseCase {

    private final PredictionMethodVersionRepository versionRepository;
    private final CalibrationProposalRepository proposalRepository;
    private final GovernanceAuditLogRepository auditLogRepository;
    private final GetPredictionEvaluationOverviewUseCase evaluationOverviewUseCase;

    @Transactional(readOnly = true)
    public GovernanceOverviewResponse execute() {
        List<PredictionMethodVersion> allVersions = versionRepository.findAll();
        List<PredictionMethodVersion> activeVersions = allVersions.stream()
                .filter(v -> v.getStatus() == MethodVersionStatus.ACTIVE)
                .collect(Collectors.toList());

        List<PredictionMethodVersion> candidateVersions = allVersions.stream()
                .filter(v -> v.getStatus() == MethodVersionStatus.CANDIDATE ||
                             v.getStatus() == MethodVersionStatus.SHADOW ||
                             v.getStatus() == MethodVersionStatus.CANARY ||
                             v.getStatus() == MethodVersionStatus.APPROVED)
                .collect(Collectors.toList());

        List<CalibrationProposal> openProposals = proposalRepository.findOpenProposals();
        List<GovernanceAuditLog> recentLogs = auditLogRepository.findRecent(20);

        PredictionEvaluationOverviewResponse evalOverview = evaluationOverviewUseCase.execute(null, null);

        int healthy = 0;
        int watch = 0;
        int degraded = 0;

        if (evalOverview != null && evalOverview.performanceByType() != null) {
            healthy = (int) evalOverview.performanceByType().stream()
                    .filter(p -> p.healthStatus() == ModelHealthStatus.HEALTHY).count();
            watch = (int) evalOverview.performanceByType().stream()
                    .filter(p -> p.healthStatus() == ModelHealthStatus.WATCH).count();
            degraded = (int) evalOverview.performanceByType().stream()
                    .filter(p -> p.healthStatus() == ModelHealthStatus.DEGRADED).count();
        }

        boolean drift = evalOverview != null && evalOverview.driftSummary() != null && evalOverview.driftSummary().driftDetected();
        String driftSummary = (evalOverview != null && evalOverview.driftSummary() != null)
                ? evalOverview.driftSummary().explanation()
                : null;

        return new GovernanceOverviewResponse(
                activeVersions.size(),
                candidateVersions.size(),
                openProposals.size(),
                healthy,
                watch,
                degraded,
                drift,
                driftSummary,
                activeVersions.stream().map(PredictionMethodVersionDto::fromDomain).collect(Collectors.toList()),
                openProposals.stream().map(CalibrationProposalDto::fromDomain).collect(Collectors.toList()),
                recentLogs.stream().map(GovernanceAuditLogDto::fromDomain).collect(Collectors.toList())
        );
    }
}
