package com.aquaai.recommendation.infrastructure.notification;

import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.port.RecommendationNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Standard notification adapter that logs high-priority recommendation alerts
 * and acts as an integration point for future SMS, email, or push notifications.
 */
@Slf4j
@Component
public class LogRecommendationNotificationAdapter implements RecommendationNotificationPort {

    @Override
    public void sendRecommendationAlert(AgronomicRecommendation recommendation) {
        if (recommendation == null) {
            return;
        }

        log.warn("AGRONOMIC RECOMMENDATION ALERT [Priority: {}] for Field {}: {} - {}",
                recommendation.getPriority(),
                recommendation.getFieldId(),
                recommendation.getTitle(),
                recommendation.getSummary()
        );
    }
}
