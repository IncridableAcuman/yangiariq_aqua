package com.aquaai.water.application;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.api.dto.CreateIrrigationEventRequest;
import com.aquaai.water.api.dto.CreateIrrigationPlanRequest;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.api.dto.UpdateWaterAllocationRequest;
import com.aquaai.water.api.dto.UpdateWaterSourceRequest;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.application.usecase.StartIrrigationEventUseCase;
import com.aquaai.water.application.usecase.CreateIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.CreateWaterAllocationUseCase;
import com.aquaai.water.application.usecase.CreateWaterSourceUseCase;
import com.aquaai.water.application.usecase.UpdateWaterAllocationUseCase;
import com.aquaai.water.application.usecase.UpdateWaterSourceUseCase;
import com.aquaai.water.domain.model.IrrigationEvent;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterUseCaseUnitTest {

    @Mock
    private WaterSourceRepository waterSourceRepository;

    @Mock
    private WaterAllocationRepository waterAllocationRepository;

    @Mock
    private IrrigationPlanRepository irrigationPlanRepository;

    @Mock
    private IrrigationEventRepository irrigationEventRepository;

    @Mock
    private FieldOwnershipChecker fieldOwnershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private final UUID farmerId = UUID.randomUUID();
    private final UUID operatorId = UUID.randomUUID();
    private final UUID fieldId = UUID.randomUUID();
    private final UUID otherFieldId = UUID.randomUUID();
    private final UUID waterSourceId = UUID.randomUUID();

    private Field testField;

    @BeforeEach
    void setUp() {
        testField = Field.create(
                farmerId,
                "Test Field",
                "Description",
                new BigDecimal("10.0000"),
                CropType.COTTON,
                SoilType.LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                null,
                null
        );
    }

    @Test
    @DisplayName("CreateWaterSource: Farmer role is rejected with 403 Forbidden")
    void shouldRejectFarmerWaterSourceCreation() {
        CreateWaterSourceUseCase useCase = new CreateWaterSourceUseCase(waterSourceRepository, currentUserProvider);

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        CreateWaterSourceRequest request = new CreateWaterSourceRequest(
                "Palvon Canal", WaterSourceType.CANAL, "Intake canal"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("Only district operators or administrators can register water sources");
    }

    @Test
    @DisplayName("CreateWaterSource: Duplicate active name is rejected with 409 Conflict")
    void shouldRejectDuplicateActiveWaterSourceName() {
        CreateWaterSourceUseCase useCase = new CreateWaterSourceUseCase(waterSourceRepository, currentUserProvider);

        when(currentUserProvider.getRequiredUserId()).thenReturn(operatorId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.OPERATOR);
        when(waterSourceRepository.existsByNameIgnoreCaseAndStatus("Palvon Canal", WaterSourceStatus.ACTIVE)).thenReturn(true);

        CreateWaterSourceRequest request = new CreateWaterSourceRequest(
                "Palvon Canal", WaterSourceType.CANAL, "Intake canal"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("CreateWaterSource: Operator can successfully register new active water source")
    void shouldCreateWaterSourceSuccessfully() {
        CreateWaterSourceUseCase useCase = new CreateWaterSourceUseCase(waterSourceRepository, currentUserProvider);

        when(currentUserProvider.getRequiredUserId()).thenReturn(operatorId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.OPERATOR);
        when(waterSourceRepository.existsByNameIgnoreCaseAndStatus("New Canal", WaterSourceStatus.ACTIVE)).thenReturn(false);
        when(waterSourceRepository.save(any(WaterSource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWaterSourceRequest request = new CreateWaterSourceRequest(
                "New Canal", WaterSourceType.CANAL, "New description"
        );

        WaterSourceResponse response = useCase.execute(request);
        assertThat(response.name()).isEqualTo("New Canal");
        assertThat(response.type()).isEqualTo(WaterSourceType.CANAL);
        assertThat(response.status()).isEqualTo(WaterSourceStatus.ACTIVE);
    }

    @Test
    @DisplayName("UpdateWaterSource: Successfully updates details for Operator")
    void shouldUpdateWaterSourceSuccessfully() {
        UpdateWaterSourceUseCase useCase = new UpdateWaterSourceUseCase(waterSourceRepository, currentUserProvider);
        WaterSource existing = WaterSource.create(waterSourceId, "Old Name", WaterSourceType.CANAL, "Old desc");

        when(currentUserProvider.getRequiredUserId()).thenReturn(operatorId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.OPERATOR);
        when(waterSourceRepository.findById(waterSourceId)).thenReturn(Optional.of(existing));
        when(waterSourceRepository.findByNameIgnoreCaseAndStatus("Updated Name", WaterSourceStatus.ACTIVE)).thenReturn(Optional.empty());
        when(waterSourceRepository.save(any(WaterSource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateWaterSourceRequest request = new UpdateWaterSourceRequest("Updated Name", WaterSourceType.RESERVOIR, "Updated desc");
        WaterSourceResponse response = useCase.execute(waterSourceId, request);

        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.type()).isEqualTo(WaterSourceType.RESERVOIR);
    }

    @Test
    @DisplayName("CreateWaterAllocation: Rejects allocating from INACTIVE water source")
    void shouldRejectAllocationFromInactiveSource() {
        CreateWaterAllocationUseCase useCase = new CreateWaterAllocationUseCase(
                waterAllocationRepository, waterSourceRepository, fieldOwnershipChecker, currentUserProvider
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);

        WaterSource inactiveSource = WaterSource.create(waterSourceId, "Dry Ditch", WaterSourceType.DITCH, null);
        inactiveSource.deactivate();
        when(waterSourceRepository.findById(waterSourceId)).thenReturn(Optional.of(inactiveSource));

        CreateWaterAllocationRequest request = new CreateWaterAllocationRequest(
                fieldId,
                waterSourceId,
                new BigDecimal("1000.0000"),
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                "Quota attempt"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot allocate water from an INACTIVE water source");
    }

    @Test
    @DisplayName("CreateWaterAllocation: Successfully creates allocation from ACTIVE source")
    void shouldCreateAllocationSuccessfully() {
        CreateWaterAllocationUseCase useCase = new CreateWaterAllocationUseCase(
                waterAllocationRepository, waterSourceRepository, fieldOwnershipChecker, currentUserProvider
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);

        WaterSource activeSource = WaterSource.create(waterSourceId, "Active Canal", WaterSourceType.CANAL, null);
        when(waterSourceRepository.findById(waterSourceId)).thenReturn(Optional.of(activeSource));
        when(waterAllocationRepository.save(any(WaterAllocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWaterAllocationRequest request = new CreateWaterAllocationRequest(
                fieldId,
                waterSourceId,
                new BigDecimal("1500.0000"),
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                "Season quota"
        );

        WaterAllocationResponse response = useCase.execute(request);
        assertThat(response.fieldId()).isEqualTo(fieldId);
        assertThat(response.waterSourceId()).isEqualTo(waterSourceId);
        assertThat(response.allocatedVolumeM3()).isEqualByComparingTo("1500.0000");
        assertThat(response.status()).isEqualTo(WaterAllocationStatus.ACTIVE);
    }

    @Test
    @DisplayName("CreateIrrigationPlan: Rejects allocation belonging to a different field (tampering)")
    void shouldRejectPlanWithCrossFieldAllocationTampering() {
        CreateIrrigationPlanUseCase useCase = new CreateIrrigationPlanUseCase(
                irrigationPlanRepository, waterAllocationRepository, fieldOwnershipChecker, currentUserProvider
        );

        UUID allocationId = UUID.randomUUID();
        // Allocation belongs to otherFieldId, but request targets fieldId
        WaterAllocation foreignAllocation = WaterAllocation.create(
                otherFieldId, waterSourceId, new BigDecimal("1000.0"), Instant.now(), Instant.now().plus(10, ChronoUnit.DAYS), null
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);
        when(waterAllocationRepository.findById(allocationId)).thenReturn(Optional.of(foreignAllocation));

        CreateIrrigationPlanRequest request = new CreateIrrigationPlanRequest(
                fieldId,
                allocationId,
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS),
                new BigDecimal("400.0000"),
                IrrigationMethod.FURROW,
                "Tampered plan"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("does not belong to field");
    }

    @Test
    @DisplayName("CreateIrrigationPlan: Rejects allocation that is CLOSED")
    void shouldRejectPlanWithClosedAllocation() {
        CreateIrrigationPlanUseCase useCase = new CreateIrrigationPlanUseCase(
                irrigationPlanRepository, waterAllocationRepository, fieldOwnershipChecker, currentUserProvider
        );

        UUID allocationId = UUID.randomUUID();
        WaterAllocation closedAllocation = WaterAllocation.create(
                fieldId, waterSourceId, new BigDecimal("1000.0"), Instant.now(), Instant.now().plus(10, ChronoUnit.DAYS), null
        );
        closedAllocation.close("Season over");

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);
        when(waterAllocationRepository.findById(allocationId)).thenReturn(Optional.of(closedAllocation));

        CreateIrrigationPlanRequest request = new CreateIrrigationPlanRequest(
                fieldId,
                allocationId,
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS),
                new BigDecimal("400.0000"),
                IrrigationMethod.FURROW,
                "Closed quota plan"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot link irrigation plan to water allocation in status CLOSED");
    }

    @Test
    @DisplayName("StartIrrigationEvent: Rejects plan belonging to a different field (tampering)")
    void shouldRejectEventWithCrossFieldPlanTampering() {
        StartIrrigationEventUseCase useCase = new StartIrrigationEventUseCase(
                irrigationEventRepository, irrigationPlanRepository, fieldOwnershipChecker, currentUserProvider
        );

        UUID planId = UUID.randomUUID();
        // Plan belongs to otherFieldId, but event targets fieldId
        IrrigationPlan foreignPlan = IrrigationPlan.create(
                otherFieldId, null, Instant.now(), Instant.now().plus(4, ChronoUnit.HOURS), new BigDecimal("300.0"), IrrigationMethod.DRIP, null
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);
        when(irrigationPlanRepository.findById(planId)).thenReturn(Optional.of(foreignPlan));

        CreateIrrigationEventRequest request = new CreateIrrigationEventRequest(
                fieldId,
                planId,
                Instant.now(),
                "Tampered event"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("does not belong to field");
    }

    @Test
    @DisplayName("StartIrrigationEvent: Successfully initiates event in STARTED status")
    void shouldStartEventSuccessfully() {
        StartIrrigationEventUseCase useCase = new StartIrrigationEventUseCase(
                irrigationEventRepository, irrigationPlanRepository, fieldOwnershipChecker, currentUserProvider
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForMutation(fieldId, farmerId, UserRole.FARMER)).thenReturn(testField);
        when(irrigationEventRepository.save(any(IrrigationEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateIrrigationEventRequest request = new CreateIrrigationEventRequest(
                fieldId,
                null,
                Instant.now(),
                "Manual pump start"
        );

        IrrigationEventResponse response = useCase.execute(request);
        assertThat(response.fieldId()).isEqualTo(fieldId);
        assertThat(response.status()).isEqualTo(com.aquaai.water.domain.model.IrrigationEventStatus.STARTED);
        assertThat(response.actualVolumeM3()).isNull();
    }
}
