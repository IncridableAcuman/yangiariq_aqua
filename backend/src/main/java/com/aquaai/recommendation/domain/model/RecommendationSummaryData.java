package com.aquaai.recommendation.domain.model;

/**
 * Value object summarizing recommendation counts across priorities and statuses.
 */
public record RecommendationSummaryData(
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
}
