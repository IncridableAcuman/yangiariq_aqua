package com.aquaai.recommendation.domain.model;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.recommendation.domain.service.ConfidenceCalculator;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecommendationDomainUnitTest {

    @Test
    @DisplayName("Lifecycle: GENERATED -> VIEWED -> ACCEPTED -> COMPLETED succeeds cleanly")
    void shouldFollowValidLifecycleTransitions() {
        UUID fieldId = UUID.randomUUID();
        UUID farmerId = UUID.randomUUID();

        AgronomicRecommendation rec = AgronomicRecommendation.create(
                fieldId,
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.HIGH,
                "Tungi sug'orish",
                "Qisqa xulosa",
                "Batafsil tushuntirish",
                BigDecimal.valueOf(0.85),
                Instant.now(),
                Instant.now().plusSeconds(86400),
                RecommendationSource.HYBRID_AI,
                "Cheklovlar yo'q",
                false,
                List.of(RecommendationEvidence.of("TELEMETRY", "SOIL_MOISTURE", BigDecimal.valueOf(14.0), BigDecimal.valueOf(25.0), "%", "Namlik kam")),
                List.of(RecommendedAction.of(1, "4 soat sug'oring", "IMMEDIATE"))
        );

        assertThat(rec.getStatus()).isEqualTo(RecommendationStatus.GENERATED);
        assertThat(rec.getConfidenceLevel()).isEqualTo(ConfidenceLevel.HIGH);

        // Step 1: Mark viewed
        rec.markViewed(farmerId);
        assertThat(rec.getStatus()).isEqualTo(RecommendationStatus.VIEWED);
        assertThat(rec.getViewedBy()).isEqualTo(farmerId);
        assertThat(rec.getViewedAt()).isNotNull();

        // Step 2: Accept
        rec.accept(farmerId);
        assertThat(rec.getStatus()).isEqualTo(RecommendationStatus.ACCEPTED);
        assertThat(rec.getAcceptedBy()).isEqualTo(farmerId);

        // Step 3: Complete
        rec.complete(farmerId);
        assertThat(rec.getStatus()).isEqualTo(RecommendationStatus.COMPLETED);
        assertThat(rec.getCompletedBy()).isEqualTo(farmerId);
        assertThat(rec.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("Terminal Invariant: Cannot transition an already COMPLETED recommendation")
    void shouldRejectTransitionFromTerminalCompleted() {
        AgronomicRecommendation rec = AgronomicRecommendation.create(
                UUID.randomUUID(),
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.HIGH,
                "Title", "Summary", "Explanation",
                BigDecimal.valueOf(0.85),
                Instant.now(), Instant.now().plusSeconds(86400),
                RecommendationSource.DETERMINISTIC_ENGINE,
                null, false, List.of(), List.of()
        );

        UUID user = UUID.randomUUID();
        rec.complete(user);
        assertThat(rec.isTerminal()).isTrue();

        assertThatThrownBy(() -> rec.dismiss(user, "Not needed"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(e -> assertThat(((BusinessRuleException) e).getErrorCode()).isEqualTo("INVALID_STATUS_TRANSITION"));

        assertThatThrownBy(() -> rec.accept(user))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(e -> assertThat(((BusinessRuleException) e).getErrorCode()).isEqualTo("INVALID_STATUS_TRANSITION"));
    }

    @Test
    @DisplayName("Dismissal Validation: Dismissing requires a non-blank reason")
    void shouldRequireReasonOnDismissal() {
        AgronomicRecommendation rec = AgronomicRecommendation.create(
                UUID.randomUUID(),
                RecommendationType.DELAY_IRRIGATION,
                RecommendationPriority.MEDIUM,
                "Title", "Summary", "Explanation",
                BigDecimal.valueOf(0.70),
                Instant.now(), Instant.now().plusSeconds(86400),
                RecommendationSource.DETERMINISTIC_ENGINE,
                null, false, List.of(), List.of()
        );

        UUID user = UUID.randomUUID();
        assertThatThrownBy(() -> rec.dismiss(user, "   "))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(e -> assertThat(((BusinessRuleException) e).getErrorCode()).isEqualTo("VALIDATION_FAILED"));

        rec.dismiss(user, "Yaqinda sug'orilgan edi");
        assertThat(rec.getStatus()).isEqualTo(RecommendationStatus.DISMISSED);
        assertThat(rec.getDismissalReason()).isEqualTo("Yaqinda sug'orilgan edi");
    }

    @Test
    @DisplayName("Confidence Clamping: Clamps outside [0.10, 0.95] into valid bounds")
    void shouldClampConfidenceScores() {
        AgronomicRecommendation recLow = AgronomicRecommendation.create(
                UUID.randomUUID(), RecommendationType.MONITOR_SOIL_MOISTURE, RecommendationPriority.LOW,
                "Title", "Summary", "Explanation",
                BigDecimal.valueOf(0.02),
                Instant.now(), Instant.now().plusSeconds(86400),
                RecommendationSource.DETERMINISTIC_ENGINE, null, false, List.of(), List.of()
        );
        assertThat(recLow.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.10));
        assertThat(recLow.getConfidenceLevel()).isEqualTo(ConfidenceLevel.LOW);

        AgronomicRecommendation recHigh = AgronomicRecommendation.create(
                UUID.randomUUID(), RecommendationType.MONITOR_SOIL_MOISTURE, RecommendationPriority.LOW,
                "Title", "Summary", "Explanation",
                BigDecimal.valueOf(0.999),
                Instant.now(), Instant.now().plusSeconds(86400),
                RecommendationSource.DETERMINISTIC_ENGINE, null, false, List.of(), List.of()
        );
        assertThat(recHigh.getConfidence()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
        assertThat(recHigh.getConfidenceLevel()).isEqualTo(ConfidenceLevel.HIGH);
    }

    @Test
    @DisplayName("ConfidenceCalculator: Derives calibrated multi-factor confidence")
    void shouldCalculateMultiFactorConfidence() {
        ConfidenceCalculator calculator = new ConfidenceCalculator();

        WeatherSnapshot weather = WeatherSnapshot.builder()
                .temperature(BigDecimal.valueOf(32.0))
                .relativeHumidity(BigDecimal.valueOf(35.0))
                .precipitationProbability(BigDecimal.ZERO)
                .timestamp(Instant.now().minusSeconds(1800))
                .source("REGIONAL_STATION")
                .build();

        AgronomicRecommendationContext context = AgronomicRecommendationContext.builder()
                .fieldId(UUID.randomUUID())
                .fieldName("Paxtakor 1")
                .cropType(CropType.COTTON)
                .soilType(SoilType.LOAM)
                .fieldAreaHectares(BigDecimal.valueOf(10.0))
                .irrigationMethod(IrrigationMethod.DRIP)
                .soilMoisturePercent(BigDecimal.valueOf(14.5))
                .soilMoistureQuality(ReadingQuality.VALID)
                .weather(weather)
                .evaluationTime(Instant.now())
                .isSimulated(false)
                .build();

        BigDecimal score = calculator.calculate(context, 2, BigDecimal.valueOf(0.85));

        assertThat(score).isGreaterThanOrEqualTo(BigDecimal.valueOf(0.70));
        assertThat(score).isLessThanOrEqualTo(BigDecimal.valueOf(0.95));
    }
}
