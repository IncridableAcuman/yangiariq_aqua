package com.aquaai.water.infrastructure.persistence;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.model.IrrigationMethod;
import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import com.aquaai.water.domain.repository.IrrigationEventRepository;
import com.aquaai.water.domain.repository.IrrigationPlanRepository;
import com.aquaai.water.domain.repository.WaterAllocationRepository;
import com.aquaai.water.domain.repository.WaterSourceRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verified PostgreSQL 16 Integration Test for Water Management & Irrigation Domain.
 * Validates against real PostgreSQL container:
 * - Liquibase execution against PostgreSQL
 * - Partial unique index: idx_water_sources_active_name (LOWER(name)) WHERE status = 'ACTIVE'
 * - NUMERIC(14, 4) decimal precision and scale
 * - Foreign key ON DELETE RESTRICT historical record protection
 */
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
class WaterPostgresDatabaseTest {

    @Autowired
    private WaterSourceRepository waterSourceRepository;

    @Autowired
    private WaterAllocationRepository waterAllocationRepository;

    @Autowired
    private IrrigationPlanRepository irrigationPlanRepository;

    @Autowired
    private IrrigationEventRepository irrigationEventRepository;

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
                .firstName("Temur")
                .lastName("Khorazmiy")
                .fullName("Temur Khorazmiy")
                .phoneNumber("+99893" + uniqueSuffix)
                .email("temur." + uniqueSuffix + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForPgWaterIntegration123456")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        postgresField = fieldRepository.save(Field.create(
                postgresFarmer.getId(),
                "Pg Cotton Field " + uniqueSuffix,
                "Irrigated parcel for pg testing",
                new BigDecimal("8.7500"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                null,
                null
        ));

        entityManager.flush();
    }

    @Test
    @DisplayName("PostgreSQL: Partial unique index prevents duplicate active water source names")
    void shouldEnforcePartialUniqueIndexOnActiveWaterSource() {
        String sourceName = "Regional Canal " + UUID.randomUUID().toString().substring(0, 8);

        // 1. Create first active water source
        waterSourceRepository.save(WaterSource.create(
                sourceName,
                WaterSourceType.CANAL,
                "Primary branch canal"
        ));
        entityManager.flush();

        // 2. Creating another ACTIVE source with the same name (case-insensitive) must violate unique partial index
        WaterSource duplicateActive = WaterSource.create(
                sourceName.toLowerCase(),
                WaterSourceType.CANAL,
                "Duplicate branch"
        );

        assertThatThrownBy(() -> {
            waterSourceRepository.save(duplicateActive);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("PostgreSQL: Allows identical name when previous water source is INACTIVE")
    void shouldAllowSameNameWhenPreviousIsInactive() {
        String sourceName = "Historical Well " + UUID.randomUUID().toString().substring(0, 8);

        WaterSource source1 = waterSourceRepository.save(WaterSource.create(
                sourceName,
                WaterSourceType.WELL,
                "Old decommissioned well"
        ));
        source1.deactivate();
        waterSourceRepository.save(source1);
        entityManager.flush();

        // New active source with identical name is permitted by partial index (WHERE status = 'ACTIVE')
        WaterSource source2 = waterSourceRepository.save(WaterSource.create(
                sourceName,
                WaterSourceType.WELL,
                "New replaced well"
        ));
        entityManager.flush();

        assertThat(source2.getId()).isNotNull();
        assertThat(source2.isActive()).isTrue();
    }

    @Test
    @DisplayName("PostgreSQL: Preserves NUMERIC(14, 4) volume precision and scale")
    void shouldPersistHighPrecisionVolumeOnPostgres() {
        WaterSource source = waterSourceRepository.save(WaterSource.create(
                "Scale Test Canal " + UUID.randomUUID().toString().substring(0, 6),
                WaterSourceType.CANAL,
                null
        ));

        BigDecimal exactVolume = new BigDecimal("9876543.1234");
        Instant start = Instant.now();
        Instant end = start.plus(30, ChronoUnit.DAYS);

        WaterAllocation allocation = waterAllocationRepository.save(WaterAllocation.create(
                postgresField.getId(),
                source.getId(),
                exactVolume,
                start,
                end,
                "High precision volume testing"
        ));
        entityManager.flush();
        entityManager.clear();

        Optional<WaterAllocation> retrieved = waterAllocationRepository.findById(allocation.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getAllocatedVolumeM3()).isEqualByComparingTo(exactVolume);
        assertThat(retrieved.get().getAllocatedVolumeM3().scale()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("PostgreSQL: Foreign key ON DELETE RESTRICT protects allocations when field is deleted")
    void shouldPreventHardDeletingFieldWithAllocations() {
        WaterSource source = waterSourceRepository.save(WaterSource.create(
                "Protected Source " + UUID.randomUUID().toString().substring(0, 6),
                WaterSourceType.CANAL,
                null
        ));

        waterAllocationRepository.save(WaterAllocation.create(
                postgresField.getId(),
                source.getId(),
                new BigDecimal("1000.0000"),
                Instant.now(),
                Instant.now().plus(10, ChronoUnit.DAYS),
                "Historical quota"
        ));
        entityManager.flush();

        // Attempting to hard-delete field must be blocked by RESTRICT constraint
        assertThatThrownBy(() -> {
            fieldRepository.deleteById(postgresField.getId());
            entityManager.flush();
        }).satisfies(thrown -> {
            assertThat(thrown).isInstanceOfAny(
                    DataIntegrityViolationException.class,
                    jakarta.persistence.PersistenceException.class,
                    org.hibernate.exception.ConstraintViolationException.class
            );
            assertThat(thrown.getMessage()).contains("fk_water_allocations_field");
        });
    }

    @Test
    @DisplayName("PostgreSQL: Complete relationship chain persists and retrieves cleanly")
    void shouldPersistFullChainOnPostgres() {
        WaterSource source = waterSourceRepository.save(WaterSource.create(
                "Chain Canal " + UUID.randomUUID().toString().substring(0, 6),
                WaterSourceType.CANAL,
                "Distribution feeder"
        ));

        Instant now = Instant.now();
        WaterAllocation allocation = waterAllocationRepository.save(WaterAllocation.create(
                postgresField.getId(),
                source.getId(),
                new BigDecimal("1200.0000"),
                now,
                now.plus(15, ChronoUnit.DAYS),
                "Allocation slot"
        ));

        IrrigationPlan plan = irrigationPlanRepository.save(IrrigationPlan.create(
                postgresField.getId(),
                allocation.getId(),
                now.plus(1, ChronoUnit.DAYS),
                now.plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("300.0000"),
                IrrigationMethod.FURROW,
                "Scheduled furrow"
        ));

        IrrigationEvent event = irrigationEventRepository.save(IrrigationEvent.recordCompleted(
                postgresField.getId(),
                plan.getId(),
                now.plus(1, ChronoUnit.DAYS),
                now.plus(1, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES),
                new BigDecimal("290.0000"),
                "Executed efficiently"
        ));
        entityManager.flush();
        entityManager.clear();

        assertThat(waterSourceRepository.findById(source.getId())).isPresent();
        assertThat(waterAllocationRepository.findById(allocation.getId())).isPresent();
        assertThat(irrigationPlanRepository.findById(plan.getId())).isPresent();
        assertThat(irrigationEventRepository.findById(event.getId())).isPresent();

        BigDecimal actualTotal = irrigationEventRepository.sumActualVolumeByFieldIdAndStatus(
                postgresField.getId(), IrrigationEventStatus.COMPLETED
        );
        assertThat(actualTotal).isEqualByComparingTo("290.0000");
    }

    @Test
    @DisplayName("PostgreSQL: findWithFilters executes without lower(bytea) syntax error when search is null or populated")
    void findWithFilters_postgresQuery_succeeds() {
        WaterSource source = waterSourceRepository.save(WaterSource.create(
                "Filter Test Canal " + UUID.randomUUID(),
                WaterSourceType.CANAL,
                "Canal notes"
        ));
        entityManager.flush();
        entityManager.clear();

        var pageNullSearch = waterSourceRepository.findWithFilters(null, null, null, 0, 20, "createdAt", "desc");
        assertThat(pageNullSearch.content()).isNotEmpty();

        var pageWithSearch = waterSourceRepository.findWithFilters("Filter Test", WaterSourceType.CANAL, WaterSourceStatus.ACTIVE, 0, 20, "createdAt", "desc");
        assertThat(pageWithSearch.content()).isNotEmpty();
        assertThat(pageWithSearch.content().stream().anyMatch(s -> s.getId().equals(source.getId()))).isTrue();

        var pageNonMatching = waterSourceRepository.findWithFilters("NonExistentCanalName123", null, null, 0, 20, "createdAt", "desc");
        assertThat(pageNonMatching.content()).isEmpty();

        var usersPage = userRepository.findUsersWithFilters(null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(usersPage).isNotNull();
    }
}
