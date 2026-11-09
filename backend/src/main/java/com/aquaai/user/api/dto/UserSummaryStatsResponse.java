package com.aquaai.user.api.dto;

import lombok.Builder;

@Builder
public record UserSummaryStatsResponse(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long blockedUsers,
        long farmersCount,
        long operatorsCount,
        long adminsCount,
        long superAdminsCount
) {
}
