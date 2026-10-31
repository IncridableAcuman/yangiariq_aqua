package com.aquaai.water.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
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
import com.aquaai.water.domain.model.IrrigationPlanStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WaterPersistenceIntegrationTest {

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

    private User testFarmer;
    private Field testField;
    private WaterSource testSource;

    @BeforeEach
    void setUp() {
        testFarmer = userRepository.save(User.builder()
                .firstName("Sobir")
                .lastName("Rahimov")
                .fullName("Sobir Rahimov")
                .phoneNumber("+998910001122")
                .email("sobir.farmer@yangiariq.uz")
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
                testFarmer.getId(),
                "Yangiariq Paxta Maydoni #1",
                "Primary cotton cultivation plot",
                new BigDecimal("12.5000"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                location,
                boundary
        ));

        testSource = waterSourceRepository.save(WaterSource.create(
                "Palvon-Yobi Main Canal",
                WaterSourceType.CANAL,
                "Key regional distribution canal supplying western Yangiariq parcels"
        ));

        entityManager.flush();
    }

    @Test
    @DisplayName("WaterSource: Persists, queries by type/status, updates, and reconstitutes")
    void shouldPersistAndQueryWaterSource() {
        WaterSource wellSource = waterSourceRepository.save(WaterSource.create(
                "Chontal Well Pumping Station",
                WaterSourceType.WELL,
                "Deep aquifer electric pumping station"
        ));
        entityManager.flush();

        Optional<WaterSource> found = waterSourceRepository.findById(wellSource.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Chontal Well Pumping Station");
        assertThat(found.get().getType()).isEqualTo(WaterSourceType.WELL);
        assertThat(found.get().getStatus()).isEqualTo(WaterSourceStatus.ACTIVE);

        List<WaterSource> canals = waterSourceRepository.findByType(WaterSourceType.CANAL);
        assertThat(canals).extracting(WaterSource::getName).contains("Palvon-Yobi Main Canal");

        Optional<WaterSource> byName = waterSourceRepository.findByNameIgnoreCaseAndStatus(
                "palvon-yobi main canal", WaterSourceStatus.ACTIVE
        );
        assertThat(byName).isPresent();
        assertThat(byName.get().getId()).isEqualTo(testSource.getId());

        assertThat(waterSourceRepository.existsByNameIgnoreCaseAndStatus("Palvon-Yobi Main Canal", WaterSourceStatus.ACTIVE)).isTrue();
        assertThat(waterSourceRepository.countByStatus(WaterSourceStatus.ACTIVE)).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("WaterAllocation: Persists allocation with BigDecimal precision and calculates volume sums")
    void shouldPersistAndQueryWaterAllocation() {
        Instant start = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(28, ChronoUnit.DAYS);
        BigDecimal volume = new BigDecimal("2500.7500");

        WaterAllocation allocation = waterAllocationRepository.save(WaterAllocation.create(
                testField.getId(),
                testSource.getId(),
                volume,
                start,
                end,
                "Seasonal Quota 2026 Western Branch"
        ));
        entityManager.flush();

        Optional<WaterAllocation> found = waterAllocationRepository.findById(allocation.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFieldId()).isEqualTo(testField.getId());
        assertThat(found.get().getWaterSourceId()).isEqualTo(testSource.getId());
        assertThat(found.get().getAllocatedVolumeM3()).isEqualByComparingTo("2500.7500");
        assertThat(found.get().getStatus()).isEqualTo(WaterAllocationStatus.ACTIVE);

        List<WaterAllocation> fieldAllocations = waterAllocationRepository.findByFieldId(testField.getId());
        assertThat(fieldAllocations).hasSize(1);

        BigDecimal sumVolume = waterAllocationRepository.sumAllocatedVolumeByFieldIdAndStatus(
                testField.getId(), WaterAllocationStatus.ACTIVE
        );
        assertThat(sumVolume).isEqualByComparingTo("2500.7500");
    }

    @Test
    @DisplayName("IrrigationPlan: Persists scheduled plan with optional allocation FK and time ranges")
    void shouldPersistAndQueryIrrigationPlan() {
        Instant planStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant planEnd = planStart.plus(8, ChronoUnit.HOURS);
        BigDecimal plannedVolume = new BigDecimal("600.0000");

        // Plan linked to quota
        WaterAllocation allocation = waterAllocationRepository.save(WaterAllocation.create(
                testField.getId(),
                testSource.getId(),
                new BigDecimal("1500.0000"),
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                null
        ));

        IrrigationPlan planWithQuota = irrigationPlanRepository.save(IrrigationPlan.create(
                testField.getId(),
                allocation.getId(),
                planStart,
                planEnd,
                plannedVolume,
                IrrigationMethod.FURROW,
                "First seasonal furrow watering"
        ));

        // Autonomous plan without quota
        IrrigationPlan planAutonomous = irrigationPlanRepository.save(IrrigationPlan.create(
                testField.getId(),
                null,
                planEnd.plus(2, ChronoUnit.DAYS),
                planEnd.plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS),
                new BigDecimal("350.0000"),
                IrrigationMethod.DRIP,
                "Autonomous supplemental drip"
        ));
        entityManager.flush();

        assertThat(irrigationPlanRepository.findById(planWithQuota.getId())).isPresent();
        assertThat(irrigationPlanRepository.findById(planAutonomous.getId())).isPresent();

        List<IrrigationPlan> fieldPlans = irrigationPlanRepository.findByFieldId(testField.getId());
        assertThat(fieldPlans).hasSize(2);

        List<IrrigationPlan> allocationPlans = irrigationPlanRepository.findByWaterAllocationId(allocation.getId());
        assertThat(allocationPlans).hasSize(1);
        assertThat(allocationPlans.get(0).getId()).isEqualTo(planWithQuota.getId());

        List<IrrigationPlan> rangePlans = irrigationPlanRepository.findByFieldIdAndPlannedStartAtBetween(
                testField.getId(),
                Instant.now(),
                Instant.now().plus(2, ChronoUnit.DAYS)
        );
        assertThat(rangePlans).hasSize(1);
        assertThat(rangePlans.get(0).getId()).isEqualTo(planWithQuota.getId());
    }

    @Test
    @DisplayName("IrrigationEvent: Persists real-time started event and transitions to completed with actual volume")
    void shouldPersistAndCompleteIrrigationEvent() {
        Instant eventStart = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant eventEnd = Instant.now().minus(10, ChronoUnit.MINUTES);
        BigDecimal actualVolume = new BigDecimal("485.2500");

        IrrigationEvent event = irrigationEventRepository.save(IrrigationEvent.start(
                testField.getId(),
                null,
                eventStart,
                "Manual siphon furrow watering"
        ));
        entityManager.flush();

        assertThat(event.getStatus()).isEqualTo(IrrigationEventStatus.STARTED);
        assertThat(event.getEndedAt()).isNull();

        // Complete the event
        event.complete(eventEnd, actualVolume, "Watering concluded satisfactorily");
        IrrigationEvent savedCompleted = irrigationEventRepository.save(event);
        entityManager.flush();

        Optional<IrrigationEvent> retrieved = irrigationEventRepository.findById(savedCompleted.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getStatus()).isEqualTo(IrrigationEventStatus.COMPLETED);
        assertThat(retrieved.get().getEndedAt()).isEqualTo(eventEnd);
        assertThat(retrieved.get().getActualVolumeM3()).isEqualByComparingTo("485.2500");
        assertThat(retrieved.get().getNotes()).contains("Watering concluded satisfactorily");

        BigDecimal totalConsumed = irrigationEventRepository.sumActualVolumeByFieldIdAndStatus(
                testField.getId(), IrrigationEventStatus.COMPLETED
        );
        assertThat(totalConsumed).isEqualByComparingTo("485.2500");
    }

    @Test
    @DisplayName("End-to-End: Full relationship chain (Field -> WaterSource -> Allocation -> Plan -> Event)")
    void shouldSupportCompleteEndToEndChain() {
        // 1. Seasonal Allocation
        Instant seasonStart = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant seasonEnd = Instant.now().plus(60, ChronoUnit.DAYS);
        WaterAllocation allocation = waterAllocationRepository.save(WaterAllocation.create(
                testField.getId(),
                testSource.getId(),
                new BigDecimal("5000.0000"),
                seasonStart,
                seasonEnd,
                "Main Canal Seasonal Quota"
        ));

        // 2. Scheduled Plan
        Instant planStart = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant planEnd = planStart.plus(6, ChronoUnit.HOURS);
        IrrigationPlan plan = irrigationPlanRepository.save(IrrigationPlan.create(
                testField.getId(),
                allocation.getId(),
                planStart,
                planEnd,
                new BigDecimal("500.0000"),
                IrrigationMethod.FURROW,
                "Furrow watering slot #1"
        ));

        // 3. Executed Event
        Instant eventStart = planStart;
        Instant eventEnd = planStart.plus(5, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES);
        IrrigationEvent event = irrigationEventRepository.save(IrrigationEvent.recordCompleted(
                testField.getId(),
                plan.getId(),
                eventStart,
                eventEnd,
                new BigDecimal("478.5000"),
                "Completed slightly ahead of schedule"
        ));
        entityManager.flush();

        // Verify navigation and linkage
        assertThat(allocation.getFieldId()).isEqualTo(testField.getId());
        assertThat(plan.getWaterAllocationId()).isEqualTo(allocation.getId());
        assertThat(event.getIrrigationPlanId()).isEqualTo(plan.getId());

        // Variance calculation
        Optional<BigDecimal> variance = event.calculateVarianceFromPlanned(plan.getPlannedVolumeM3());
        assertThat(variance).isPresent();
        assertThat(variance.get()).isEqualByComparingTo("21.5000"); // 500 - 478.5 = 21.5 m³ saved
    }

    @Test
    @DisplayName("Foreign Key Integrity: Non-existent fieldId throws DataIntegrityViolationException")
    void shouldEnforceFieldForeignKey() {
        UUID nonExistentFieldId = UUID.randomUUID();
        WaterAllocation orphanAllocation = WaterAllocation.create(
                nonExistentFieldId,
                testSource.getId(),
                new BigDecimal("100.0"),
                Instant.now(),
                Instant.now().plus(5, ChronoUnit.DAYS),
                null
        );

        assertThatThrownBy(() -> {
            waterAllocationRepository.save(orphanAllocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Foreign Key Integrity: Non-existent waterSourceId throws DataIntegrityViolationException")
    void shouldEnforceWaterSourceForeignKey() {
        UUID nonExistentSourceId = UUID.randomUUID();
        WaterAllocation orphanAllocation = WaterAllocation.create(
                testField.getId(),
                nonExistentSourceId,
                new BigDecimal("100.0"),
                Instant.now(),
                Instant.now().plus(5, ChronoUnit.DAYS),
                null
        );

        assertThatThrownBy(() -> {
            waterAllocationRepository.save(orphanAllocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
