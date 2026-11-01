package com.aquaai.intelligence.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.AgronomicSignal;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;
import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;
import com.aquaai.intelligence.domain.port.AgronomicAssessmentRepository;
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
class IntelligencePersistenceIntegrationTest {

    @Autowired
    private AgronomicAssessmentRepository assessmentRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Field testField;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .firstName("Anvar")
                .lastName("Qodirov")
                .fullName("Anvar Qodirov")
                .phoneNumber("+998901239988")
                .email("intel_farmer" + UUID.randomUUID().toString().substring(0, 8) + "@aquaai.uz")
                .passwordHash("$2a$10$hashedTestingPasswordForIntelligence123")
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
                "Intelligence Test Parcel",
                "Parcel for intelligence persistence verification",
                new BigDecimal("8.0000"),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                location,
                boundary
        ));
    }

    @Test
    @DisplayName("Should persist and retrieve AgronomicAssessment with supporting signals")
    void shouldPersistAndRetrieveAssessment() {
        Instant now = Instant.now();
        AgronomicSignal signal1 = AgronomicSignal.of(
                AgronomicSignalType.SOIL_TOO_DRY,
                IntelligenceSeverity.CRITICAL,
                "SOIL_MOISTURE",
                new BigDecimal("13.4"),
                new BigDecimal("15.0"),
                "Critical soil moisture deficit"
        );
        AgronomicSignal signal2 = AgronomicSignal.of(
                AgronomicSignalType.HIGH_TEMPERATURE,
                IntelligenceSeverity.HIGH,
                "TEMPERATURE",
                new BigDecimal("41.2"),
                new BigDecimal("40.0"),
                "Ambient heat wave active"
        );

        TelemetryTrend trend = new TelemetryTrend(
                TrendDirection.DECREASING,
                new BigDecimal("-0.8500"),
                4,
                now.minus(4, ChronoUnit.HOURS),
                now
        );

        AgronomicAssessment assessment = new AgronomicAssessment(
                UUID.randomUUID(),
                testField.getId(),
                AgronomicSignalType.SOIL_TOO_DRY,
                IntelligenceSeverity.CRITICAL,
                trend,
                TelemetryFreshness.FRESH,
                IntelligenceConfidence.HIGH,
                new BigDecimal("0.8900"),
                "Deterministic evaluation: severe root zone drying.",
                "Controlled night irrigation recommended.",
                false,
                null,
                false,
                null,
                now,
                now,
                List.of(signal1, signal2)
        );

        AgronomicAssessment saved = assessmentRepository.save(assessment);
        assertThat(saved.getId()).isEqualTo(assessment.getId());

        Optional<AgronomicAssessment> retrievedOpt = assessmentRepository.findLatestByFieldId(testField.getId());
        assertThat(retrievedOpt).isPresent();

        AgronomicAssessment retrieved = retrievedOpt.get();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getFieldId()).isEqualTo(testField.getId());
        assertThat(retrieved.getPrimarySignal()).isEqualTo(AgronomicSignalType.SOIL_TOO_DRY);
        assertThat(retrieved.getOverallSeverity()).isEqualTo(IntelligenceSeverity.CRITICAL);
        assertThat(retrieved.getSoilMoistureTrend().getDirection()).isEqualTo(TrendDirection.DECREASING);
        assertThat(retrieved.getSignals()).hasSize(2);
        assertThat(retrieved.isHydraulicSafetyBlocked()).isFalse();

        // Paginated retrieval
        List<AgronomicAssessment> list = assessmentRepository.findByFieldId(testField.getId(), null, null, 0, 10);
        assertThat(list).hasSize(1);
        long count = assessmentRepository.countByFieldId(testField.getId(), null, null);
        assertThat(count).isEqualTo(1);
    }
}
