package com.aquaai.recommendation.application.service;

import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.recommendation.domain.model.AIRecommendationResponse;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.RecommendationEvidence;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.model.RecommendedAction;
import com.aquaai.recommendation.domain.port.AgronomicAIProvider;
import com.aquaai.recommendation.domain.port.AgronomicRecommendationEngine;
import com.aquaai.recommendation.domain.port.RecommendationNotificationPort;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.recommendation.domain.service.ConfidenceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrator service implementing the hybrid AI/deterministic agronomic recommendation lifecycle:
 * <ol>
 *   <li>Builds holistic cross-bounded agronomic context.</li>
 *   <li>Executes deterministic rule engine for hydraulic interlocks and agricultural safety rules.</li>
 *   <li>Consults advisory AI provider for agronomic reasoning and explanation synthesis.</li>
 *   <li>Calibrates multi-factor confidence.</li>
 *   <li>Enforces 4-hour cooldown deduplication and priority escalation.</li>
 *   <li>Dispatches notification alerts for critical and high priority recommendations.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationOrchestrationService {

    private static final Duration COOLDOWN_WINDOW = Duration.ofHours(4);

    private final RecommendationContextBuilder contextBuilder;
    private final AgronomicRecommendationEngine deterministicEngine;
    private final AgronomicAIProvider aiProvider;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationNotificationPort notificationPort;
    private final FieldRepository fieldRepository;

    private final ConfidenceCalculator confidenceCalculator = new ConfidenceCalculator();

    @Transactional
    public AgronomicRecommendation generateForField(UUID fieldId) {
        log.info("Generating agronomic recommendation for field parcel {}", fieldId);
        AgronomicRecommendationContext context = contextBuilder.buildContext(fieldId);

        // 1. Evaluate deterministic safety rules
        List<AgronomicRecommendation> deterministicCandidates = deterministicEngine.evaluate(context);

        // Find pipe burst or critical hydraulic anomaly if present
        Optional<AgronomicRecommendation> pipeBurstCandidate = deterministicCandidates.stream()
                .filter(c -> c.getRecommendationType() == RecommendationType.INSPECT_PIPE_BURST)
                .findFirst();

        Optional<AgronomicRecommendation> criticalCandidate = deterministicCandidates.stream()
                .filter(c -> c.getPriority() == RecommendationPriority.CRITICAL)
                .findFirst();

        AgronomicRecommendation deterministicCandidate = pipeBurstCandidate
                .or(() -> criticalCandidate)
                .or(() -> deterministicCandidates.isEmpty() ? Optional.empty() : Optional.of(deterministicCandidates.get(0)))
                .orElseGet(() -> AgronomicRecommendation.create(
                        fieldId,
                        RecommendationType.MONITOR_SOIL_MOISTURE,
                        RecommendationPriority.INFO,
                        "Maintain Standard Monitoring",
                        "Telemetry within expected ranges.",
                        "No immediate agronomic actions required.",
                        BigDecimal.valueOf(0.80),
                        Instant.now(),
                        Instant.now().plus(48, ChronoUnit.HOURS),
                        RecommendationSource.DETERMINISTIC_ENGINE,
                        null,
                        context.isSimulated(),
                        List.of(),
                        List.of()
                ));

        // 2. Hydraulic safety interlock: if pipe burst or critical pressure drop, strictly enforce inspection
        boolean isHydraulicInterlock = pipeBurstCandidate.isPresent() || criticalCandidate.isPresent();

        AgronomicRecommendation finalRecommendation;

        if (isHydraulicInterlock) {
            log.warn("Hydraulic interlock activated for field {}: using deterministic safety recommendation {}",
                    fieldId, deterministicCandidate.getRecommendationType());
            finalRecommendation = deterministicCandidate;
        } else {
            // Attempt AI advisory consultation
            Optional<AIRecommendationResponse> aiResponseOpt = aiProvider.generateRecommendation(context);

            if (aiResponseOpt.isPresent()) {
                AIRecommendationResponse aiResponse = aiResponseOpt.get();

                // Derive evidence from context metrics
                List<RecommendationEvidence> evidence = new ArrayList<>(deterministicCandidate.getEvidence());

                // Build actions list
                List<RecommendedAction> actions = new ArrayList<>();
                int order = 1;
                if (aiResponse.actions() != null) {
                    for (String actionText : aiResponse.actions()) {
                        actions.add(RecommendedAction.of(order++, actionText, aiResponse.priority().name()));
                    }
                }
                if (aiResponse.doNotActions() != null) {
                    for (String doNotText : aiResponse.doNotActions()) {
                        actions.add(RecommendedAction.doNot(order++, doNotText));
                    }
                }

                // Multi-factor confidence calibration
                BigDecimal calibratedConfidence = confidenceCalculator.calculate(
                        context,
                        evidence.size(),
                        aiResponse.confidenceScore() != null ? aiResponse.confidenceScore() : BigDecimal.valueOf(0.80)
                );

                Instant now = Instant.now();
                finalRecommendation = AgronomicRecommendation.create(
                        fieldId,
                        aiResponse.recommendationType(),
                        aiResponse.priority(),
                        aiResponse.title(),
                        aiResponse.summary(),
                        aiResponse.explanation(),
                        calibratedConfidence,
                        now,
                        now.plus(48, ChronoUnit.HOURS),
                        RecommendationSource.HYBRID_AI,
                        aiResponse.limitations(),
                        context.isSimulated(),
                        evidence,
                        actions
                );
            } else {
                log.info("Advisory AI provider returned empty. Falling back to deterministic recommendation.");
                finalRecommendation = deterministicCandidate;
            }
        }

        // 3. Deduplication & Cooldown Check
        Optional<AgronomicRecommendation> existingActiveOpt = recommendationRepository.findActiveByFieldAndType(
                fieldId, finalRecommendation.getRecommendationType()
        );

        if (existingActiveOpt.isPresent()) {
            AgronomicRecommendation existing = existingActiveOpt.get();
            Duration age = Duration.between(existing.getGeneratedAt(), Instant.now());

            boolean isPriorityEscalated = finalRecommendation.getPriority().compareTo(existing.getPriority()) < 0;

            if (age.compareTo(COOLDOWN_WINDOW) < 0 && !isPriorityEscalated) {
                log.info("Recommendation {} for field {} is within 4-hour cooldown ({}) and priority not escalated. Returning existing active.",
                        existing.getRecommendationType(), fieldId, age);
                return existing;
            }

            // If past cooldown or priority escalated, expire old recommendation
            existing.expire(Instant.now());
            recommendationRepository.save(existing);
        }

        // 4. Save and notify
        AgronomicRecommendation saved = recommendationRepository.save(finalRecommendation);
        if (saved.getPriority() == RecommendationPriority.CRITICAL || saved.getPriority() == RecommendationPriority.HIGH) {
            notificationPort.sendRecommendationAlert(saved);
        }

        return saved;
    }

    @Transactional
    public List<AgronomicRecommendation> evaluateAllFields() {
        log.info("Evaluating agronomic recommendations across all registered field parcels");
        List<Field> fields = fieldRepository.findAll();
        List<AgronomicRecommendation> results = new ArrayList<>();

        for (Field field : fields) {
            try {
                AgronomicRecommendation rec = generateForField(field.getId());
                results.add(rec);
            } catch (Exception e) {
                log.error("Failed to generate recommendation for field {}: {}", field.getId(), e.getMessage());
            }
        }

        return results;
    }

    @Transactional
    public int expireStaleRecommendations() {
        Instant now = Instant.now();
        List<AgronomicRecommendation> expired = recommendationRepository.findExpiredActiveRecommendations(now);
        for (AgronomicRecommendation rec : expired) {
            rec.expire(now);
            recommendationRepository.save(rec);
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} stale recommendations past validity window", expired.size());
        }
        return expired.size();
    }
}
