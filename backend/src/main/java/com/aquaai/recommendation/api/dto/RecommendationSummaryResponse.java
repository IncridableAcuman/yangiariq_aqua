package com.aquaai.recommendation.api.dto;

import com.aquaai.recommendation.domain.model.RecommendationSummaryData;

public record RecommendationSummaryResponse(
        long totalActive,
        long criticalCount,
        long highCount,
        long mediumCount,
        long lowCount,
        long infoCount,
        long generatedCount,
        long viewedCount,
        long acceptedCount,
        long dismissedCount,
        long completedCount,
        long expiredCount
) {
    public static RecommendationSummaryResponse fromDomain(RecommendationSummaryData data) {
        if (data == null) {
            return new RecommendationSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        return new RecommendationSummaryResponse(
                data.totalActive(),
                data.criticalCount(),
                data.highCount(),
                data.mediumCount(),
                data.lowCount(),
                data.infoCount(),
                data.generatedCount(),
                data.viewedCount(),
                data.acceptedCount(),
                data.dismissedCount(),
                data.completedCount(),
                data.expiredCount()
        );
    }
}
