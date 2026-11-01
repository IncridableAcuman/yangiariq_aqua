package com.aquaai.recommendation.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for continuous automated agronomic monitoring and cleanup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.recommendations.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class RecommendationSchedulerService {

    private final RecommendationOrchestrationService orchestrationService;

    /**
     * Periodically auto-expires recommendations whose validity window has elapsed.
     * Runs every 15 minutes.
     */
    @Scheduled(fixedDelayString = "${app.recommendations.expiry-interval-ms:900000}")
    public void runExpiryCleanup() {
        try {
            orchestrationService.expireStaleRecommendations();
        } catch (Exception e) {
            log.error("Error during scheduled recommendation expiry cleanup: {}", e.getMessage(), e);
        }
    }
}
