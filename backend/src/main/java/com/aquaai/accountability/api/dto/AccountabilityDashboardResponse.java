package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.application.dto.AccountabilityDashboardSummary;

public record AccountabilityDashboardResponse(
        long totalIncidents,
        long openIncidents,
        long criticalIncidents,
        long underInvestigation,
        long overdueIncidents,
        long pendingVerification,
        long resolvedIncidents,
        long reopenedIncidents
) {
    public static AccountabilityDashboardResponse from(AccountabilityDashboardSummary s) {
        if (s == null) return null;
        return new AccountabilityDashboardResponse(
                s.totalIncidents(),
                s.openIncidents(),
                s.criticalIncidents(),
                s.underInvestigation(),
                s.overdueIncidents(),
                s.pendingVerification(),
                s.resolvedIncidents(),
                s.reopenedIncidents()
        );
    }
}
