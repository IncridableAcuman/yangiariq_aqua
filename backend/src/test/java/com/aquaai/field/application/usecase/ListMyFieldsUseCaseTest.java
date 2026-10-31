package com.aquaai.field.application.usecase;

import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldPage;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.api.PageResponse;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMyFieldsUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ListMyFieldsUseCase listMyFieldsUseCase;

    private UUID farmerId;
    private Field field1;

    @BeforeEach
    void setUp() {
        farmerId = UUID.randomUUID();
        field1 = Field.create(
                farmerId,
                "Field 1",
                null,
                new BigDecimal("5.0"),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.FURROW,
                null,
                null
        );
    }

    @Test
    @DisplayName("Farmer user automatically queries only their own fields, ignoring passed filterOwnerId")
    void shouldQueryOnlyOwnFieldsForFarmer() {
        UUID externalFarmerId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);

        FieldPage page = new FieldPage(List.of(field1), 0, 20, 1L, 1);
        when(fieldRepository.findWithFilters(
                eq(farmerId),
                eq(FieldStatus.ACTIVE),
                eq(CropType.COTTON),
                eq(null),
                eq(null),
                eq(0),
                eq(20),
                eq("createdAt"),
                eq("desc")
        )).thenReturn(page);

        PageResponse<FieldResponse> response = listMyFieldsUseCase.execute(
                FieldStatus.ACTIVE,
                CropType.COTTON,
                null,
                null,
                externalFarmerId, // Should be overridden to farmerId
                0,
                20,
                "createdAt",
                "desc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().name()).isEqualTo("Field 1");
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();

        verify(fieldRepository).findWithFilters(
                eq(farmerId), // Verified farmerId was used instead of externalFarmerId
                eq(FieldStatus.ACTIVE),
                eq(CropType.COTTON),
                eq(null),
                eq(null),
                eq(0),
                eq(20),
                eq("createdAt"),
                eq("desc")
        );
    }

    @Test
    @DisplayName("Admin user can filter by a specific owner ID or query district-wide")
    void shouldAllowAdminToFilterByOwner() {
        UUID adminId = UUID.randomUUID();
        UUID targetFarmerId = UUID.randomUUID();
        when(currentUserProvider.getRequiredUserId()).thenReturn(adminId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.ADMIN);

        FieldPage page = new FieldPage(List.of(field1), 0, 20, 1L, 1);
        when(fieldRepository.findWithFilters(
                eq(targetFarmerId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(0),
                eq(20),
                eq("name"),
                eq("asc")
        )).thenReturn(page);

        PageResponse<FieldResponse> response = listMyFieldsUseCase.execute(
                null,
                null,
                null,
                null,
                targetFarmerId,
                0,
                20,
                "name",
                "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(fieldRepository).findWithFilters(
                eq(targetFarmerId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(0),
                eq(20),
                eq("name"),
                eq("asc")
        );
    }
}
