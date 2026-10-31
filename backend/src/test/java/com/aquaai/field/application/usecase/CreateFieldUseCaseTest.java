package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.DuplicateResourceException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFieldUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CreateFieldUseCase createFieldUseCase;

    private UUID farmerId;
    private CreateFieldRequest validRequest;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        validRequest = new CreateFieldRequest(
                "Paxtakor 1",
                "High quality cotton parcel",
                new BigDecimal("10.5000"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                new BigDecimal("41.450000"),
                new BigDecimal("60.590000"),
                null
        );
    }

    @Test
    @DisplayName("Farmer successfully creates a field with valid parameters")
    void shouldCreateFieldSuccessfully() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(userRepository.existsById(farmerId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(farmerId, "Paxtakor 1", FieldStatus.ACTIVE))
                .thenReturn(false);

        Field savedField = Field.create(
                farmerId,
                validRequest.name(),
                validRequest.description(),
                validRequest.areaHectares(),
                validRequest.cropType(),
                validRequest.soilType(),
                validRequest.irrigationMethod(),
                null,
                null
        );
        when(fieldRepository.save(any(Field.class))).thenReturn(savedField);

        FieldResponse response = createFieldUseCase.execute(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Paxtakor 1");
        assertThat(response.cropType()).isEqualTo(CropType.COTTON);
        assertThat(response.status()).isEqualTo(FieldStatus.ACTIVE);
        verify(fieldRepository).save(any(Field.class));
    }

    @Test
    @DisplayName("Non-farmer role is rejected when attempting to create a field")
    void shouldRejectNonFarmerRole() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.OPERATOR);

        assertThatThrownBy(() -> createFieldUseCase.execute(validRequest))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("Only registered farmers can create agricultural field parcels");
    }

    @Test
    @DisplayName("Non-existent owner user throws ResourceNotFoundException")
    void shouldRejectNonExistentUser() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(userRepository.existsById(farmerId)).thenReturn(false);

        assertThatThrownBy(() -> createFieldUseCase.execute(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Duplicate active field name for same farmer throws DuplicateResourceException")
    void shouldRejectDuplicateActiveFieldName() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(userRepository.existsById(farmerId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(farmerId, "Paxtakor 1", FieldStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> createFieldUseCase.execute(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists for this farmer");
    }

    @Test
    @DisplayName("Domain validation: Zero or negative area is rejected")
    void shouldRejectInvalidAreaInDomain() {
        CreateFieldRequest invalidAreaRequest = new CreateFieldRequest(
                "Bog' 1",
                null,
                BigDecimal.ZERO,
                CropType.WHEAT,
                SoilType.SANDY,
                IrrigationMethod.CANAL,
                null,
                null,
                null
        );

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(userRepository.existsById(farmerId)).thenReturn(true);
        when(fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(farmerId, "Bog' 1", FieldStatus.ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> createFieldUseCase.execute(invalidAreaRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("greater than zero hectares");
    }
}
