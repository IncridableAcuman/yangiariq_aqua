package com.aquaai.field.application;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldOwnershipCheckerTest {

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private FieldOwnershipChecker ownershipChecker;

    private UUID fieldId;
    private UUID ownerId;
    private UUID otherUserId;
    private Field testField;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        testField = Field.create(
                ownerId,
                "Field 1",
                "Description",
                new BigDecimal("10.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.CANAL,
                null,
                null
        );
    }

    @Test
    @DisplayName("Direct owner passes verification successfully")
    void shouldPassVerificationForOwner() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(testField));

        assertThatCode(() -> ownershipChecker.verifyOwnership(fieldId, ownerId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Non-owner user fails verification with UnauthorizedOperationException")
    void shouldRejectNonOwner() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(testField));

        assertThatThrownBy(() -> ownershipChecker.verifyOwnership(fieldId, otherUserId))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("You do not have ownership of this field");
    }

    @Test
    @DisplayName("Non-existent field throws ResourceNotFoundException")
    void shouldThrowWhenFieldNotFound() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownershipChecker.verifyOwnership(fieldId, ownerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Field not found");
    }

    @Test
    @DisplayName("Admin or Operator override bypasses direct ownership check")
    void shouldAllowAdminOrOperatorOverride() {
        when(fieldRepository.existsById(fieldId)).thenReturn(true);

        assertThatCode(() -> ownershipChecker.verifyOwnershipOrRole(
                fieldId, otherUserId, UserRole.ADMIN, UserRole.ADMIN, UserRole.OPERATOR))
                .doesNotThrowAnyException();

        assertThatCode(() -> ownershipChecker.verifyOwnershipOrRole(
                fieldId, otherUserId, UserRole.OPERATOR, UserRole.ADMIN, UserRole.OPERATOR))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Farmer role cannot bypass ownership check via override method")
    void shouldNotAllowFarmerBypassOverride() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(testField));

        assertThatThrownBy(() -> ownershipChecker.verifyOwnershipOrRole(
                fieldId, otherUserId, UserRole.FARMER, UserRole.ADMIN, UserRole.OPERATOR))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    @DisplayName("isOwner returns true only when repository confirms ownership")
    void shouldCheckIsOwnerAccurately() {
        when(fieldRepository.existsByOwnerIdAndId(ownerId, fieldId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndId(otherUserId, fieldId)).thenReturn(false);

        assertThat(ownershipChecker.isOwner(fieldId, ownerId)).isTrue();
        assertThat(ownershipChecker.isOwner(fieldId, otherUserId)).isFalse();
        assertThat(ownershipChecker.isOwner(null, ownerId)).isFalse();
        assertThat(ownershipChecker.isOwner(fieldId, null)).isFalse();
    }
}
