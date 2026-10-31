package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
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
class ReactivateFieldUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker ownershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ReactivateFieldUseCase reactivateFieldUseCase;

    private UUID fieldId;
    private UUID ownerId;
    private Field inactiveField;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        inactiveField = Field.create(
                fieldId,
                ownerId,
                "Restorable Field",
                null,
                new BigDecimal("4.5"),
                CropType.MELON,
                SoilType.SANDY,
                IrrigationMethod.DRIP,
                null,
                null
        );
        inactiveField.deactivate();
    }

    @Test
    @DisplayName("Owner successfully reactivates field when name does not conflict with another active field")
    void shouldReactivateFieldSuccessfully() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER)).thenReturn(inactiveField);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, "Restorable Field", FieldStatus.ACTIVE, fieldId)).thenReturn(false);
        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FieldResponse response = reactivateFieldUseCase.execute(fieldId);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(FieldStatus.ACTIVE);
        assertThat(inactiveField.isActive()).isTrue();
        verify(fieldRepository).save(inactiveField);
    }

    @Test
    @DisplayName("Reactivating a field with a name that now belongs to another active field throws DuplicateResourceException")
    void shouldRejectReactivationOnNameConflict() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER)).thenReturn(inactiveField);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatusAndIdNot(
                ownerId, "Restorable Field", FieldStatus.ACTIVE, fieldId)).thenReturn(true);

        assertThatThrownBy(() -> reactivateFieldUseCase.execute(fieldId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("Non-owner cannot reactivate another farmer's field (ResourceNotFoundException)")
    void shouldRejectNonOwner() {
        UUID intruderId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(intruderId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        doThrow(new ResourceNotFoundException("Field", fieldId))
                .when(ownershipChecker).getFieldForMutation(fieldId, intruderId, UserRole.FARMER);

        assertThatThrownBy(() -> reactivateFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }

    @Test
    @DisplayName("Reactivating a non-existent field throws ResourceNotFoundException")
    void shouldThrowWhenFieldNotFound() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", fieldId));

        assertThatThrownBy(() -> reactivateFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }
}
