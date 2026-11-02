package com.aquaai.analytics.infrastructure.persistence;

import com.aquaai.analytics.domain.port.AnalyticsQueryPort;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.infrastructure.persistence.SmartIrrigationPlanJpaEntity;
import com.aquaai.irrigation.infrastructure.persistence.SpringDataSmartIrrigationPlanRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
class AnalyticsPostgresDatabaseTest {

    @Autowired
    private AnalyticsQueryPort analyticsQueryPort;

    @Autowired
    private SpringDataSmartIrrigationPlanRepository smartIrrigationPlanRepository;

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
                .firstName("Sobir")
                .lastName("Khorazmiy")
                .fullName("Sobir Khorazmiy")
                .phoneNumber("+99894" + uniqueSuffix)
                .email("sobir." + uniqueSuffix + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForPgAnalyticsIntegration123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        postgresField = fieldRepository.save(Field.create(
                postgresFarmer.getId(),
                "Analytics Pg Field " + uniqueSuffix,
                "Irrigated parcel for pg analytics testing",
                new BigDecimal("12.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                null,
                null
        ));

        // Create a SmartIrrigationPlan
        Instant now = Instant.now();
        SmartIrrigationPlanJpaEntity plan = SmartIrrigationPlanJpaEntity.builder()
                .id(UUID.randomUUID())
                .fieldId(postgresField.getId())
                .status(IrrigationPlanStatus.APPROVED)
                .priority(IrrigationPriority.HIGH)
                .plannedDate(LocalDate.now())
                .plannedStartAt(now.plus(2, ChronoUnit.HOURS))
                .plannedEndAt(now.plus(4, ChronoUnit.HOURS))
                .recommendedVolumeM3(new BigDecimal("250.0000"))
                .estimatedDurationMinutes(120)
                .currentMoisturePercent(new BigDecimal("22.50"))
                .targetMoisturePercent(new BigDecimal("35.00"))
                .reason("Optimal schedule based on moisture levels")
                .source("DETERMINISTIC_SCHEDULER")
                .createdAt(now)
                .updatedAt(now)
                .build();

        entityManager.persist(plan);
        entityManager.flush();
    }

    @Test
    @DisplayName("PostgreSQL: getPlanStatusCounts executes without 42P18 parameter data type error when date filters are null")
    void shouldExecutePlanStatusCountsWithNullDatesAgainstPostgres() {
        Map<UUID, Map<IrrigationPlanStatus, Long>> counts = analyticsQueryPort.getPlanStatusCounts(
                List.of(postgresField.getId()),
                null,
                null
        );

        assertThat(counts).containsKey(postgresField.getId());
        assertThat(counts.get(postgresField.getId()).get(IrrigationPlanStatus.APPROVED)).isEqualTo(1L);
    }

    @Test
    @DisplayName("PostgreSQL: getPlanStatusCounts executes with non-null date filters against Postgres")
    void shouldExecutePlanStatusCountsWithNonNullDatesAgainstPostgres() {
        Map<UUID, Map<IrrigationPlanStatus, Long>> counts = analyticsQueryPort.getPlanStatusCounts(
                List.of(postgresField.getId()),
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(7)
        );

        assertThat(counts).containsKey(postgresField.getId());
        assertThat(counts.get(postgresField.getId()).get(IrrigationPlanStatus.APPROVED)).isEqualTo(1L);
    }

    @Test
    @DisplayName("PostgreSQL: getPlannedVolumes executes without 42P18 parameter data type error when date filters are null")
    void shouldExecutePlannedVolumesWithNullDatesAgainstPostgres() {
        Map<UUID, BigDecimal> volumes = analyticsQueryPort.getPlannedVolumes(
                List.of(postgresField.getId()),
                null,
                null
        );

        assertThat(volumes).containsKey(postgresField.getId());
        assertThat(volumes.get(postgresField.getId())).isEqualByComparingTo(new BigDecimal("250.0000"));
    }

    @Test
    @DisplayName("PostgreSQL: SpringDataSmartIrrigationPlanRepository.findFiltered executes without parameter data type error")
    void shouldExecuteFindFilteredWithNullsAgainstPostgres() {
        var page = smartIrrigationPlanRepository.findFiltered(
                postgresField.getId(),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }
}
