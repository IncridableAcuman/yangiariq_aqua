package com.aquaai.prediction.application.dto;

import java.util.List;

public record GovernanceOverviewResponse(
        int activeVersionsCount,
        int candidateVersionsCount,
        int openProposalsCount,
        int healthyCount,
        int watchCount,
        int degradedCount,
        boolean driftWarningActive,
        String driftSummary,
        List<PredictionMethodVersionDto> activeVersions,
        List<CalibrationProposalDto> openProposals,
        List<GovernanceAuditLogDto> recentAuditLogs
) {
}
