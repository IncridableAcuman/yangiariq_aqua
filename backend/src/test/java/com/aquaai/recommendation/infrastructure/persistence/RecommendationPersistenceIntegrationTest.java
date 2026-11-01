package com.aquaai.recommendation.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationEvidence;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationSummaryData;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.model.RecommendedAction;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecommendationPersistenceIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Field testField;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .firstName("Sobir")
                .lastName("Rahimov")
                .fullName("Sobir Rahimov")
                .phoneNumber("+998910001122")
                .email("rec_farmer" + UUID.randomUUID().toString().substring(0, 8) + "@aquaai.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForWaterIntegration123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.5800, 41.4400),
                CoordinatePoint.of(60.5850, 41.4400),
                CoordinatePoint.of(60.5850, 41.4450),
                CoordinatePoint.of(60.5800, 41.4450)
        ));
        FieldLocation location = FieldLocation.of(41.442500, 60.582500);

        testField = fieldRepository.save(Field.create(
                testUser.getId(),
                "Parcel Alpha",
                "Primary test field",
                new BigDecimal("12.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                location,
                boundary
        ));
    }

    @Test
    @DisplayName("Should save and retrieve recommendation with evidence and actions")
    void shouldSaveAndRetrieveRecommendation() {
        RecommendationEvidence evidence1 = RecommendationEvidence.of(
                "SOIL_SENSOR", "SOIL_MOISTURE", new BigDecimal("13.2"), new BigDecimal("22.0"), "%", "Moisture below PWP"
        );
        RecommendedAction action1 = RecommendedAction.of(1, "Execute furrow irrigation with 450 m3/ha", "IMMEDIATE");
        RecommendedAction action2 = RecommendedAction.doNot(2, "Do not apply nitrogen fertilizer while soil is dehydrated");

        AgronomicRecommendation recommendation = AgronomicRecommendation.create(
                testField.getId(),
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.CRITICAL,
                "Immediate Irrigation Required",
                "Soil moisture dropped to 13.2%, approaching permanent wilting point.",
                "Detailed agronomic explanation: cotton requires immediate hydration.",
                new BigDecimal("0.92"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.HYBRID_AI,
                "Provisional regional parameters",
                false,
                List.of(evidence1),
                List.of(action1, action2)
        );

        AgronomicRecommendation saved = recommendationRepository.save(recommendation);
        assertThat(saved.getId()).isEqualTo(recommendation.getId());

        Optional<AgronomicRecommendation> retrievedOpt = recommendationRepository.findById(saved.getId());
        assertThat(retrievedOpt).isPresent();

        AgronomicRecommendation retrieved = retrievedOpt.get();
        assertThat(retrieved.getTitle()).isEqualTo("Immediate Irrigation Required");
        assertThat(retrieved.getRecommendationType()).isEqualTo(RecommendationType.IRRIGATE_NOW);
        assertThat(retrieved.getPriority()).isEqualTo(RecommendationPriority.CRITICAL);
        assertThat(retrieved.getStatus()).isEqualTo(RecommendationStatus.GENERATED);
        assertThat(retrieved.getEvidence()).hasSize(1);
        assertThat(retrieved.getEvidence().get(0).getMetric()).isEqualTo("SOIL_MOISTURE");
        assertThat(retrieved.getActions()).hasSize(2);
        assertThat(retrieved.getActions().get(1).isNegativeAction()).isTrue();
    }

    @Test
    @DisplayName("Should find active recommendation by field and type")
    void shouldFindActiveRecommendationByFieldAndType() {
        AgronomicRecommendation rec = AgronomicRecommendation.create(
                testField.getId(),
                RecommendationType.DELAY_IRRIGATION,
                RecommendationPriority.MEDIUM,
                "Postpone Irrigation",
                "Heavy rain expected.",
                "Weather forecast indicates 22mm precipitation.",
                new BigDecimal("0.85"),
                Instant.now(),
                Instant.now().plus(48, ChronoUnit.HOURS),
                RecommendationSource.DETERMINISTIC_ENGINE,
                null,
                false,
                List.of(),
                List.of()
        );
        recommendationRepository.save(rec);

        Optional<AgronomicRecommendation> active = recommendationRepository.findActiveByFieldAndType(
                testField.getId(), RecommendationType.DELAY_IRRIGATION
        );
        assertThat(active).isPresent();
        assertThat(active.get().getId()).isEqualTo(rec.getId());

        // When dismissed, should no longer be active
        active.get().dismiss(testUser.getId(), "Weather forecast changed, rain bypassed the area");
        recommendationRepository.save(active.get());

        Optional<AgronomicRecommendation> activeAfterDismiss = recommendationRepository.findActiveByFieldAndType(
                testField.getId(), RecommendationType.DELAY_IRRIGATION
        );
        assertThat(activeAfterDismiss).isEmpty();
    }

    @Test
    @DisplayName("Should aggregate recommendation summary data correctly")
    void shouldAggregateRecommendationSummary() {
        AgronomicRecommendation rec1 = AgronomicRecommendation.create(
                testField.getId(),
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.CRITICAL,
                "Irrigate Now",
                "Deficit",
                "Detailed",
                new BigDecimal("0.90"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.HYBRID_AI,
                null,
                false,
                List.of(),
                List.of()
        );

        AgronomicRecommendation rec2 = AgronomicRecommendation.create(
                testField.getId(),
                RecommendationType.MANAGE_SALINITY,
                RecommendationPriority.HIGH,
                "Salinity Alert",
                "High EC",
                "Detailed",
                new BigDecimal("0.80"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.DETERMINISTIC_ENGINE,
                null,
                false,
                List.of(),
                List.of()
        );

        recommendationRepository.save(rec1);
        recommendationRepository.save(rec2);

        RecommendationSummaryData summary = recommendationRepository.getSummary(testField.getId());
        assertThat(summary.totalActive()).isEqualTo(2);
        assertThat(summary.criticalCount()).isEqualTo(1);
        assertThat(summary.highCount()).isEqualTo(1);
        assertThat(summary.generatedCount()).isEqualTo(2);
    }
}
