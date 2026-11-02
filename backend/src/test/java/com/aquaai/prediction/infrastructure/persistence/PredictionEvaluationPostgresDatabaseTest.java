package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.Socket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/yangiariq_aqua",
        "spring.datasource.username=aqua_user",
        "spring.datasource.password=aqua_secure_password_dev",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true"
})
@Transactional
@EnabledIf("isPostgresAvailable")
class PredictionEvaluationPostgresDatabaseTest {

    @Autowired
    private PredictionEvaluationRepository evaluationRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User postgresFarmer;
    private Field postgresField;

    static boolean isPostgresAvailable() {
        try (Socket socket = new Socket("localhost", 5433)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        postgresFarmer = userRepository.save(User.builder()
                .firstName("Alisher")
                .lastName("Navoiy")
                .fullName("Alisher Navoiy")
                .phoneNumber("+99891" + uniqueSuffix)
                .email("alisher." + uniqueSuffix + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForPgEvalIntegration123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        postgresField = fieldRepository.save(Field.create(
                postgresFarmer.getId(),
                "Pg Eval Field " + uniqueSuffix,
                "Irrigated parcel for pg evaluation testing",
                new BigDecimal("15.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.DRIP,
                null,
                null
        ));

        entityManager.flush();
    }

    @Test
    @DisplayName("PostgreSQL: Persisting prediction evaluation succeeds and can be queried")
    void shouldPersistAndQueryEvaluationInPostgres() {
        UUID predId = UUID.randomUUID();
        Instant now = Instant.now();

        Prediction pred = Prediction.builder()
                .id(predId)
                .fieldId(postgresField.getId())
                .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .targetTime(now.minus(1, ChronoUnit.HOURS))
                .generatedAt(now.minus(25, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("26.50"))
                .lowerBound(new BigDecimal("23.00"))
                .upperBound(new BigDecimal("30.00"))
                .confidencePercent(84)
                .riskLevel(RiskLevel.LOW)
                .build();
        predictionRepository.save(pred);
        entityManager.flush();

        PredictionEvaluation eval = PredictionEvaluation.createNumerical(
                predId,
                postgresField.getId(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                "DETERMINISTIC_FAO56_V1",
                now.minus(25, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS),
                new BigDecimal("26.50"),
                new BigDecimal("23.00"),
                new BigDecimal("30.00"),
                new BigDecimal("27.00"),
                null,
                null,
                null,
                "Matched sensor reading",
                false,
                84,
                EvaluationStatus.EVALUATED,
                EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE,
                "NEAREST_OBSERVATION",
                "notes"
        );
        evaluationRepository.save(eval);
        entityManager.flush();

        Optional<PredictionEvaluation> found = evaluationRepository.findByPredictionId(predId);
        assertThat(found).isPresent();
        assertThat(found.get().getAbsoluteError()).isEqualByComparingTo(new BigDecimal("0.5000"));
        assertThat(found.get().getIsWithinBounds()).isTrue();
    }

    @Test
    @DisplayName("PostgreSQL: Unique constraint enforces idempotency (duplicate evaluation rejected)")
    void shouldEnforceUniqueConstraintOnPredictionId() {
        UUID predId = UUID.randomUUID();
        Instant now = Instant.now();

        Prediction pred = Prediction.builder()
                .id(predId)
                .fieldId(postgresField.getId())
                .predictionType(PredictionType.WATER_DEMAND_FORECAST)
                .horizon(PredictionHorizon.NEXT_48_HOURS)
                .targetTime(now)
                .generatedAt(now.minus(48, ChronoUnit.HOURS))
                .predictedValue(new BigDecimal("300.00"))
                .confidencePercent(80)
                .riskLevel(RiskLevel.MEDIUM)
                .build();
        predictionRepository.save(pred);
        entityManager.flush();

        PredictionEvaluation eval1 = PredictionEvaluation.createNumerical(
                predId, postgresField.getId(), PredictionType.WATER_DEMAND_FORECAST, PredictionHorizon.NEXT_48_HOURS,
                "WATER_BUDGET_V1", now.minus(48, ChronoUnit.HOURS), now, now, new BigDecimal("300.00"),
                new BigDecimal("280.00"), new BigDecimal("320.00"), new BigDecimal("290.00"),
                null, null, null, null, false, 80, EvaluationStatus.EVALUATED, EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE, "TEST", "notes"
        );
        evaluationRepository.save(eval1);
        entityManager.flush();

        // Attempting to save a second evaluation for the same prediction_id should throw DataIntegrityViolationException
        PredictionEvaluation eval2 = PredictionEvaluation.createNumerical(
                predId, postgresField.getId(), PredictionType.WATER_DEMAND_FORECAST, PredictionHorizon.NEXT_48_HOURS,
                "WATER_BUDGET_V1", now.minus(48, ChronoUnit.HOURS), now, now, new BigDecimal("300.00"),
                new BigDecimal("280.00"), new BigDecimal("320.00"), new BigDecimal("290.00"),
                null, null, null, null, false, 80, EvaluationStatus.EVALUATED, EvaluationFailureReason.NONE,
                EvaluationDataQuality.DATA_COMPLETE, "TEST", "duplicate"
        );

        assertThatThrownBy(() -> {
            evaluationRepository.save(eval2);
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class);
    }
}
