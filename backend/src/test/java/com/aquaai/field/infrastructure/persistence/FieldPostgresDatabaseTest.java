package com.aquaai.field.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import com.aquaai.user.domain.UserStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verified PostgreSQL Integration Test.
 * Runs against the actual PostgreSQL 16 container, validating:
 * - Liquibase execution against PostgreSQL
 * - JSONB column storage and querying
 * - Foreign key constraints
 * - PostgreSQL partial unique index: idx_fields_owner_active_name (owner_id, LOWER(name)) WHERE status = 'ACTIVE'
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
class FieldPostgresDatabaseTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User postgresFarmer;

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
                .firstName("Jaloliddin")
                .lastName("Manguberdi")
                .fullName("Jaloliddin Manguberdi")
                .phoneNumber("+99890" + uniqueSuffix)
                .email("jaloliddin." + uniqueSuffix + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForPgTesting123456789")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
    }

    @Test
    @DisplayName("PostgreSQL: Persists field with JSONB boundary, verifies PostgreSQL JSONB storage")
    void shouldPersistWithPostgresJsonb() {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.5910, 41.4510),
                CoordinatePoint.of(60.5960, 41.4510),
                CoordinatePoint.of(60.5960, 41.4560),
                CoordinatePoint.of(60.5910, 41.4560)
        ));

        FieldLocation location = FieldLocation.of(41.453500, 60.593500);

        Field field = Field.create(
                postgresFarmer.getId(),
                "Yangiariq PG Plot 1",
                "Stored in PostgreSQL with JSONB boundary",
                new BigDecimal("22.7500"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                location,
                boundary
        );

        Field saved = fieldRepository.save(field);
        assertThat(saved.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Optional<Field> reloaded = fieldRepository.findById(saved.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getName()).isEqualTo("Yangiariq PG Plot 1");
        assertThat(reloaded.get().getBoundary()).isNotNull();
        assertThat(reloaded.get().getBoundary().type()).isEqualTo("Polygon");
        assertThat(reloaded.get().getBoundary().coordinates()).hasSize(1);
        assertThat(reloaded.get().getLocation()).isNotNull();
        assertThat(reloaded.get().getLocation().latitude()).isEqualByComparingTo("41.453500");
        assertThat(reloaded.get().getLocation().longitude()).isEqualByComparingTo("60.593500");
    }

    @Test
    @DisplayName("PostgreSQL: Database enforces unique partial index on (owner_id, LOWER(name)) for ACTIVE fields")
    void shouldEnforcePostgresUniqueActiveNameIndex() {
        Field field1 = Field.create(
                postgresFarmer.getId(),
                "Katta Dala",
                "First active field",
                new BigDecimal("10.0000"),
                CropType.WHEAT,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
                null,
                null
        );
        fieldRepository.save(field1);
        entityManager.flush();

        // Attempt to insert another active field with the exact same name (different case) directly into DB
        Field field2 = Field.create(
                postgresFarmer.getId(),
                "katta dala",
                "Duplicate active name attempt",
                new BigDecimal("12.0000"),
                CropType.RICE,
                SoilType.CLAY,
                IrrigationMethod.CANAL,
                null,
                null
        );

        assertThatThrownBy(() -> {
            fieldRepository.save(field2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("PostgreSQL: Foreign key cascades on user deletion")
    void shouldCascadeDeleteInPostgres() {
        Field field = Field.create(
                postgresFarmer.getId(),
                "Temporary PG Parcel",
                null,
                new BigDecimal("5.0"),
                CropType.MAIZE,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
        Field saved = fieldRepository.save(field);
        entityManager.flush();
        assertThat(fieldRepository.existsById(saved.getId())).isTrue();

        // Delete user
        userRepository.deleteById(postgresFarmer.getId());
        userRepository.flush();
        entityManager.clear();

        // ON DELETE CASCADE removes field in PostgreSQL
        assertThat(fieldRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("PostgreSQL: Query-level ownership findByIdAndOwnerId strictly isolates parcels in PostgreSQL")
    void shouldEnforceQueryLevelOwnershipInPostgres() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User secondFarmer = userRepository.save(User.builder()
                .firstName("Second")
                .lastName("Farmer")
                .fullName("Second Farmer")
                .phoneNumber("+99893" + uniqueSuffix)
                .email("second." + uniqueSuffix + "@yangiariq.uz")
                .passwordHash("$2a$10$validPasswordHashForPgTesting123456789")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        Field fieldA = fieldRepository.save(Field.create(
                postgresFarmer.getId(),
                "Farmer A PG Field",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        ));

        Field fieldB = fieldRepository.save(Field.create(
                secondFarmer.getId(),
                "Farmer B PG Field",
                null,
                new BigDecimal("12.0"),
                CropType.WHEAT,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null
        ));

        entityManager.flush();
        entityManager.clear();

        // 1. Farmer A queries own Field A -> Found
        assertThat(fieldRepository.findByIdAndOwnerId(fieldA.getId(), postgresFarmer.getId())).isPresent();

        // 2. Farmer A queries Field B -> Empty (anti-enumeration in PostgreSQL)
        assertThat(fieldRepository.findByIdAndOwnerId(fieldB.getId(), postgresFarmer.getId())).isEmpty();

        // 3. Farmer B queries own Field B -> Found
        assertThat(fieldRepository.findByIdAndOwnerId(fieldB.getId(), secondFarmer.getId())).isPresent();

        // 4. Farmer B queries Field A -> Empty (anti-enumeration in PostgreSQL)
        assertThat(fieldRepository.findByIdAndOwnerId(fieldA.getId(), secondFarmer.getId())).isEmpty();
    }
}
