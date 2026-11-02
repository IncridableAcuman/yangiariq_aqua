package com.aquaai.prediction.application.dto;

import java.util.List;

public record CalibrationProposalDetailResponse(
        CalibrationProposalDto proposal,
        BacktestRunDto latestBacktestRun,
        CandidateComparisonDto comparison,
        List<GovernanceAuditLogDto> auditHistory
) {
}
