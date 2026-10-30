package com.aquaai.field.application;

import com.aquaai.field.application.dto.CreateFieldCommand;
import com.aquaai.field.application.dto.FieldResult;
import com.aquaai.field.application.dto.UpdateFieldCommand;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldServiceUnitTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker ownershipChecker;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FieldService fieldService;

    private UUID ownerId;
    private UUID fieldId;
    private Field testField;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        testField = Field.create(
                ownerId,
                "Paxta 1",
                "Description",
                new BigDecimal("8.0000"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
                FieldLocation.of(41.450000, 60.590000),
                null
        );
    }

    @Test
    @DisplayName("createField successfully registers a field when owner exists and name is unique")
    void shouldCreateFieldSuccessfully() {
        CreateFieldCommand cmd = new CreateFieldCommand(
                ownerId,
                "Paxta 1",
                "Description",
                new BigDecimal("8.0000"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
                FieldLocation.of(41.450000, 60.590000),
                null
        );

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(ownerId, "Paxta 1", FieldStatus.ACTIVE))
                .thenReturn(false);
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        FieldResult result = fieldService.createField(cmd);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Paxta 1");
        assertThat(result.ownerId()).isEqualTo(ownerId);
        assertThat(result.cropType()).isEqualTo(CropType.COTTON);
        verify(fieldRepository).save(any(Field.class));
    }

    @Test
    @DisplayName("createField rejects non-existent user identity")
    void shouldRejectNonExistentUser() {
        CreateFieldCommand cmd = new CreateFieldCommand(
                ownerId,
                "Paxta 1",
                "Description",
                new BigDecimal("8.0"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
                null,
                null
        );

        when(userRepository.existsById(ownerId)).thenReturn(false);

        assertThatThrownBy(() -> fieldService.createField(cmd))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("createField rejects duplicate active field name for same farmer")
    void shouldRejectDuplicateActiveFieldName() {
        CreateFieldCommand cmd = new CreateFieldCommand(
                ownerId,
                "Paxta 1",
                "Description",
                new BigDecimal("8.0"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                IrrigationMethod.FURROW,
                null,
                null
        );

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(ownerId, "Paxta 1", FieldStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> fieldService.createField(cmd))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("updateField enforces ownership verification before updating")
    void shouldEnforceOwnershipOnUpdate() {
        UUID intruderId = UUID.randomUUID();
        UpdateFieldCommand cmd = new UpdateFieldCommand(
                fieldId,
                intruderId,
                "New Name",
                "New Desc",
                new BigDecimal("10.0"),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );

        doThrow(new UnauthorizedOperationException("Access denied: You do not have ownership of this field"))
                .when(ownershipChecker).verifyOwnership(fieldId, intruderId);

        assertThatThrownBy(() -> fieldService.updateField(cmd))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("You do not have ownership of this field");
    }

    @Test
    @DisplayName("updateField rejects duplicate name if another active field of same farmer has it")
    void shouldRejectDuplicateNameOnUpdate() {
        UpdateFieldCommand cmd = new UpdateFieldCommand(
                testField.getId(),
                ownerId,
                "Existing Field Name",
                "New Desc",
                new BigDecimal("10.0"),
                CropType.WHEAT,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );

        when(fieldRepository.findById(testField.getId())).thenReturn(Optional.of(testField));
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, "Existing Field Name", FieldStatus.ACTIVE, testField.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> fieldService.updateField(cmd))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("deactivateField marks field as INACTIVE")
    void shouldDeactivateField() {
        when(fieldRepository.findById(testField.getId())).thenReturn(Optional.of(testField));
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        fieldService.deactivateField(testField.getId(), ownerId);

        verify(ownershipChecker).verifyOwnership(testField.getId(), ownerId);
        assertThat(testField.getStatus()).isEqualTo(FieldStatus.INACTIVE);
        verify(fieldRepository).save(testField);
    }

    @Test
    @DisplayName("activateField rejects activation if name conflicts with another active field")
    void shouldRejectActivationOnConflict() {
        testField.deactivate();
        when(fieldRepository.findById(testField.getId())).thenReturn(Optional.of(testField));
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, testField.getName(), FieldStatus.ACTIVE, testField.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> fieldService.activateField(testField.getId(), ownerId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("listFieldsByOwner returns all fields for that owner")
    void shouldListFieldsByOwner() {
        when(fieldRepository.findByOwnerId(ownerId)).thenReturn(List.of(testField));

        List<FieldResult> results = fieldService.listFieldsByOwner(ownerId);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).isEqualTo("Paxta 1");
    }
}
