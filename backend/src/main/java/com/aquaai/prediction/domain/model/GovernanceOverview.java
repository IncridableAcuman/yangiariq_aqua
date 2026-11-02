package com.aquaai.prediction.domain.model;

import java.util.List;

/**
 * Summary view of platform predictive governance health, versions, proposals, and audit logs.
 */
public record GovernanceOverview(
        int activeVersionsCount,
        int candidateVersionsCount,
        int openProposalsCount,
        int healthyCount,
        int watchCount,
        int degradedCount,
        boolean driftWarningActive,
        String driftSummary,
        List<PredictionMethodVersion> activeVersions,
        List<CalibrationProposal> openProposals,
        List<GovernanceAuditLog> recentAuditLogs
) {
}
