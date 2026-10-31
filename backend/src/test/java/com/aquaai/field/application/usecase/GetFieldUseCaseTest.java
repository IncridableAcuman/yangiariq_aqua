package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
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
class GetFieldUseCaseTest {

    @Mock
    private FieldOwnershipChecker ownershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private GetFieldUseCase getFieldUseCase;

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
                "Paxta Maydoni",
                "Description",
                new BigDecimal("8.0000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
    }

    @Test
    @DisplayName("Owner farmer successfully retrieves own field")
    void shouldRetrieveFieldForOwner() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForRead(fieldId, ownerId, UserRole.FARMER)).thenReturn(testField);

        FieldResponse response = getFieldUseCase.execute(fieldId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(fieldId);
        assertThat(response.name()).isEqualTo("Paxta Maydoni");
        verify(ownershipChecker).getFieldForRead(fieldId, ownerId, UserRole.FARMER);
    }

    @Test
    @DisplayName("Non-owner farmer requesting another's field is rejected with ResourceNotFoundException (anti-enumeration)")
    void shouldRejectNonOwnerFarmer() {
        UUID intruderId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(intruderId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        doThrow(new ResourceNotFoundException("Field", fieldId))
                .when(ownershipChecker).getFieldForRead(fieldId, intruderId, UserRole.FARMER);

        assertThatThrownBy(() -> getFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }

    @Test
    @DisplayName("Operator successfully retrieves field across district")
    void shouldAllowOperatorAccess() {
        UUID operatorId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(operatorId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.OPERATOR);
        when(ownershipChecker.getFieldForRead(fieldId, operatorId, UserRole.OPERATOR)).thenReturn(testField);

        FieldResponse response = getFieldUseCase.execute(fieldId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(fieldId);
        verify(ownershipChecker).getFieldForRead(fieldId, operatorId, UserRole.OPERATOR);
    }

    @Test
    @DisplayName("Non-existent field throws ResourceNotFoundException")
    void shouldThrowWhenFieldNotFound() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(ownerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(ownershipChecker.getFieldForRead(fieldId, ownerId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", fieldId));

        assertThatThrownBy(() -> getFieldUseCase.execute(fieldId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }
}
