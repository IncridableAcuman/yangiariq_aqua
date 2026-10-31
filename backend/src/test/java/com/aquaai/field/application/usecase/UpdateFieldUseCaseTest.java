package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.api.dto.UpdateFieldRequest;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFieldUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker ownershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UpdateFieldUseCase updateFieldUseCase;

    private UUID fieldId;
    private UUID ownerId;
    private Field existingField;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        existingField = Field.create(
                fieldId,
                ownerId,
                "Original Name",
                "Original Description",
                new BigDecimal("10.0000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
    }

    @Test
    @DisplayName("Owner successfully updates field with PATCH semantics: unprovided fields remain unchanged")
    void shouldUpdateFieldWithPatchSemantics() {
        UpdateFieldRequest patchRequest = new UpdateFieldRequest(
                "Updated Name",
                null, // Should remain "Original Description"
                null, // Should remain 10.0000
                CropType.RICE,
                null, // Should remain LOAM
                null, // Should remain CANAL
                null,
                null,
                null
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER)).thenReturn(existingField);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, "Updated Name", FieldStatus.ACTIVE, fieldId)).thenReturn(false);
        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FieldResponse response = updateFieldUseCase.execute(fieldId, patchRequest);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.description()).isEqualTo("Original Description"); // Preserved!
        assertThat(response.areaHectares()).isEqualByComparingTo("10.0000"); // Preserved!
        assertThat(response.cropType()).isEqualTo(CropType.RICE); // Updated!
        assertThat(response.soilType()).isEqualTo(SoilType.LOAM); // Preserved!
        assertThat(response.irrigationMethod()).isEqualTo(IrrigationMethod.CANAL); // Preserved!
        verify(fieldRepository).save(existingField);
    }

    @Test
    @DisplayName("Non-owner is rejected with ResourceNotFoundException (anti-enumeration)")
    void shouldRejectNonOwner() {
        UUID intruderId = UUID.randomUUID();
        UpdateFieldRequest request = new UpdateFieldRequest("New", null, null, null, null, null, null, null, null);

        when(currentUserProvider.getRequiredUserId()).thenReturn(intruderId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        doThrow(new ResourceNotFoundException("Field", fieldId))
                .when(ownershipChecker).getFieldForMutation(fieldId, intruderId, UserRole.FARMER);

        assertThatThrownBy(() -> updateFieldUseCase.execute(fieldId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }

    @Test
    @DisplayName("Updating name to an already existing active field name for same owner throws DuplicateResourceException")
    void shouldRejectDuplicateNameOnUpdate() {
        UpdateFieldRequest request = new UpdateFieldRequest("Existing Active Field", null, null, null, null, null, null, null, null);

        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER)).thenReturn(existingField);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, "Existing Active Field", FieldStatus.ACTIVE, fieldId)).thenReturn(true);

        assertThatThrownBy(() -> updateFieldUseCase.execute(fieldId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("Updating a non-existent field throws ResourceNotFoundException")
    void shouldThrowWhenFieldNotFound() {
        UpdateFieldRequest request = new UpdateFieldRequest("New", null, null, null, null, null, null, null, null);

        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", fieldId));

        assertThatThrownBy(() -> updateFieldUseCase.execute(fieldId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }
}
