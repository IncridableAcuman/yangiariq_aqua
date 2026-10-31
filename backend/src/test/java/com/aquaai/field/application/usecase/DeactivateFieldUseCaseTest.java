package com.aquaai.field.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateFieldUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker ownershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private DeactivateFieldUseCase deactivateFieldUseCase;

    private UUID fieldId;
    private UUID ownerId;
    private Field testField;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        testField = Field.create(
                fieldId,
                ownerId,
                "Field to Deactivate",
                null,
                new BigDecimal("6.0"),
                CropType.WHEAT,
                SoilType.SANDY_LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
    }

    @Test
    @DisplayName("Owner successfully deactivates field, setting status to INACTIVE without hard deletion")
    void shouldDeactivateFieldSuccessfully() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER)).thenReturn(testField);

        deactivateFieldUseCase.execute(fieldId);

        assertThat(testField.getStatus()).isEqualTo(FieldStatus.INACTIVE);
        assertThat(testField.isInactive()).isTrue();
        verify(fieldRepository).save(testField);
        verify(ownershipChecker).getFieldForMutation(fieldId, ownerId, UserRole.FARMER);
    }

    @Test
    @DisplayName("Non-owner cannot deactivate another farmer's field (ResourceNotFoundException)")
    void shouldRejectNonOwner() {
        UUID intruderId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(intruderId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        doThrow(new ResourceNotFoundException("Field", fieldId))
                .when(ownershipChecker).getFieldForMutation(fieldId, intruderId, UserRole.FARMER);

        assertThatThrownBy(() -> deactivateFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }

    @Test
    @DisplayName("Deactivating non-existent field throws ResourceNotFoundException")
    void shouldThrowWhenFieldNotFound() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForMutation(fieldId, ownerId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", fieldId));

        assertThatThrownBy(() -> deactivateFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }
}
