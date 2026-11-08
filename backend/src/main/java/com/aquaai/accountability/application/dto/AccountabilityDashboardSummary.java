package com.aquaai.accountability.application.dto;

public record AccountabilityDashboardSummary(
        long totalIncidents,
        long openIncidents,
        long criticalIncidents,
        long underInvestigation,
        long overdueIncidents,
        long pendingVerification,
        long resolvedIncidents,
        long reopenedIncidents
) {}
