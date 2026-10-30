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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FieldPersistenceIntegrationTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testFarmer;
    private User secondFarmer;

    @BeforeEach
    void setUp() {
        testFarmer = userRepository.save(User.builder()
                .firstName("Alisher")
                .lastName("Navoiy")
                .fullName("Alisher Navoiy")
                .phoneNumber("+998901112233")
                .email("alisher.farmer@yangiariq.uz")
                .passwordHash("$2a$10$hashedpasswordforintegrationtesting123")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());

        secondFarmer = userRepository.save(User.builder()
                .firstName("Bobur")
                .lastName("Mirzo")
                .fullName("Bobur Mirzo")
                .phoneNumber("+998904445566")
                .email("bobur.farmer@yangiariq.uz")
                .passwordHash("$2a$10$hashedpasswordforintegrationtesting456")
                .role(UserRole.FARMER)
                .status(UserStatus.ACTIVE)
                .district("Yangiariq")
                .build());
    }

    @Test
    @DisplayName("Field aggregate persists and reconstitutes accurately with all properties")
    void shouldPersistAndReconstituteField() {
        FieldBoundary boundary = FieldBoundary.fromExteriorRing(List.of(
                CoordinatePoint.of(60.5900, 41.4500),
                CoordinatePoint.of(60.5950, 41.4500),
                CoordinatePoint.of(60.5950, 41.4550),
                CoordinatePoint.of(60.5900, 41.4550)
        ));

        FieldLocation location = FieldLocation.of(41.452500, 60.592500);

        Field field = Field.create(
                testFarmer.getId(),
                "Yangiariq Bog' 1",
                "High fertility cotton parcel",
                new BigDecimal("14.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                location,
                boundary
        );

        Field saved = fieldRepository.save(field);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        Optional<Field> loadedOpt = fieldRepository.findById(saved.getId());
        assertThat(loadedOpt).isPresent();

        Field loaded = loadedOpt.get();
        assertThat(loaded.getId()).isEqualTo(saved.getId());
        assertThat(loaded.getOwnerId()).isEqualTo(testFarmer.getId());
        assertThat(loaded.getName()).isEqualTo("Yangiariq Bog' 1");
        assertThat(loaded.getDescription()).isEqualTo("High fertility cotton parcel");
        assertThat(loaded.getAreaHectares()).isEqualByComparingTo("14.5000");
        assertThat(loaded.getCropType()).isEqualTo(CropType.COTTON);
        assertThat(loaded.getSoilType()).isEqualTo(SoilType.LOAM);
        assertThat(loaded.getIrrigationMethod()).isEqualTo(IrrigationMethod.FURROW);
        assertThat(loaded.getStatus()).isEqualTo(FieldStatus.ACTIVE);
        assertThat(loaded.getLocation()).isNotNull();
        assertThat(loaded.getLocation().latitude()).isEqualByComparingTo("41.452500");
        assertThat(loaded.getLocation().longitude()).isEqualByComparingTo("60.592500");
        assertThat(loaded.getBoundary()).isNotNull();
        assertThat(loaded.getBoundary().type()).isEqualTo("Polygon");
        assertThat(loaded.getBoundary().coordinates()).hasSize(1);
        assertThat(loaded.getBoundary().coordinates().getFirst()).hasSize(5); // 4 points + closed loop
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Lookup by owner ID isolates farmer fields")
    void shouldFindFieldsByOwner() {
        Field field1 = Field.create(
                testFarmer.getId(),
                "Alisher Field A",
                null,
                new BigDecimal("5.0"),
                CropType.WHEAT,
                SoilType.SANDY_LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );

        Field field2 = Field.create(
                secondFarmer.getId(),
                "Bobur Field B",
                null,
                new BigDecimal("7.5"),
                CropType.MELON,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null
        );

        fieldRepository.save(field1);
        fieldRepository.save(field2);

        List<Field> alisherFields = fieldRepository.findByOwnerId(testFarmer.getId());
        assertThat(alisherFields).hasSize(1);
        assertThat(alisherFields.getFirst().getName()).isEqualTo("Alisher Field A");

        List<Field> boburFields = fieldRepository.findByOwnerId(secondFarmer.getId());
        assertThat(boburFields).hasSize(1);
        assertThat(boburFields.getFirst().getName()).isEqualTo("Bobur Field B");
    }

    @Test
    @DisplayName("Owner-scoped active field name uniqueness query functions correctly")
    void shouldCheckOwnerScopedNameUniqueness() {
        Field field1 = Field.create(
                testFarmer.getId(),
                "Dala 1",
                null,
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.CLAY_LOAM,
                IrrigationMethod.FURROW,
                null,
                null
        );
        fieldRepository.save(field1);

        // Same owner, same name, case-insensitive
        assertThat(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                testFarmer.getId(), "dala 1", FieldStatus.ACTIVE)).isTrue();

        // Same owner, different name
        assertThat(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                testFarmer.getId(), "dala 2", FieldStatus.ACTIVE)).isFalse();

        // Different owner can use same name
        assertThat(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                secondFarmer.getId(), "dala 1", FieldStatus.ACTIVE)).isFalse();
    }

    @Test
    @DisplayName("Inactive field does not conflict with active field name check")
    void shouldNotConflictWhenFieldInactive() {
        Field field1 = Field.create(
                testFarmer.getId(),
                "Makkajo'xori Dala",
                null,
                new BigDecimal("6.0"),
                CropType.MAIZE,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
        field1.deactivate();
        fieldRepository.save(field1);

        // Querying for ACTIVE name should return false since it is INACTIVE
        assertThat(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                testFarmer.getId(), "Makkajo'xori Dala", FieldStatus.ACTIVE)).isFalse();

        // Querying for INACTIVE name should return true
        assertThat(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(
                testFarmer.getId(), "Makkajo'xori Dala", FieldStatus.INACTIVE)).isTrue();
    }

    @Test
    @DisplayName("Foreign key cascade: deleting the owner user cascades to deleting their fields")
    void shouldCascadeDeleteWhenUserDeleted() {
        Field field = Field.create(
                testFarmer.getId(),
                "Temporary Field",
                null,
                new BigDecimal("3.0"),
                CropType.RICE,
                SoilType.CLAY,
                IrrigationMethod.CANAL,
                null,
                null
        );
        Field saved = fieldRepository.save(field);
        assertThat(fieldRepository.existsById(saved.getId())).isTrue();

        // Delete user
        userRepository.deleteById(testFarmer.getId());
        userRepository.flush();
        entityManager.clear();

        // Field should be gone via ON DELETE CASCADE
        assertThat(fieldRepository.findById(saved.getId())).isEmpty();
    }
}
