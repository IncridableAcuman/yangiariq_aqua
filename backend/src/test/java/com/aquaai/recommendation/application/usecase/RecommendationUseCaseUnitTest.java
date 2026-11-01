package com.aquaai.recommendation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.recommendation.application.service.RecommendationOrchestrationService;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationSource;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.RecommendationRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationUseCaseUnitTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker fieldOwnershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private RecommendationOrchestrationService orchestrationService;

    private UUID farmerUserId;
    private UUID otherFarmerUserId;
    private UUID testFieldId;
    private Field testField;
    private AgronomicRecommendation sampleRecommendation;

    @BeforeEach
    void setUp() {
        farmerUserId = UUID.randomUUID();
        otherFarmerUserId = UUID.randomUUID();
        testFieldId = UUID.randomUUID();

        testField = Field.create(
                farmerUserId,
                "Yangiariq Parcel 101",
                "Testing plot",
                new BigDecimal("10.00"),
                CropType.COTTON,
                SoilType.SANDY_LOAM,
                com.aquaai.field.domain.model.IrrigationMethod.FURROW,
                FieldLocation.of(41.34, 60.62),
                FieldBoundary.fromExteriorRing(List.of(
                        CoordinatePoint.of(60.62, 41.34),
                        CoordinatePoint.of(60.63, 41.34),
                        CoordinatePoint.of(60.63, 41.35),
                        CoordinatePoint.of(60.62, 41.35)
                ))
        );

        sampleRecommendation = AgronomicRecommendation.create(
                testFieldId,
                RecommendationType.IRRIGATE_NOW,
                RecommendationPriority.HIGH,
                "Irrigate Cotton Plot",
                "Moisture depletion detected",
                "Explanation",
                new BigDecimal("0.88"),
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                RecommendationSource.HYBRID_AI,
                null,
                false,
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("GenerateRecommendationUseCase executes orchestration service when authorized")
    void shouldGenerateRecommendationWhenAuthorized() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);
        when(orchestrationService.generateForField(testFieldId)).thenReturn(sampleRecommendation);

        GenerateRecommendationUseCase useCase = new GenerateRecommendationUseCase(
                orchestrationService, fieldOwnershipChecker, currentUserProvider
        );

        AgronomicRecommendation result = useCase.execute(testFieldId);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Irrigate Cotton Plot");
        verify(orchestrationService).generateForField(testFieldId);
    }

    @Test
    @DisplayName("GetRecommendationUseCase throws ResourceNotFoundException (Anti-IDOR) for unauthorized farmer")
    void shouldThrow404WhenFarmerDoesNotOwnField() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(otherFarmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, otherFarmerUserId, UserRole.FARMER))
                .thenThrow(new ResourceNotFoundException("Field", testFieldId));

        GetRecommendationUseCase useCase = new GetRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        assertThatThrownBy(() -> useCase.execute(sampleRecommendation.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recommendation");
    }

    @Test
    @DisplayName("ViewRecommendationUseCase marks recommendation viewed")
    void shouldMarkRecommendationViewed() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);
        when(recommendationRepository.save(any(AgronomicRecommendation.class))).thenAnswer(i -> i.getArgument(0));

        ViewRecommendationUseCase useCase = new ViewRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        AgronomicRecommendation viewed = useCase.execute(sampleRecommendation.getId());
        assertThat(viewed.getStatus()).isEqualTo(RecommendationStatus.VIEWED);
        assertThat(viewed.getViewedBy()).isEqualTo(farmerUserId);
        assertThat(viewed.getViewedAt()).isNotNull();
    }

    @Test
    @DisplayName("AcceptRecommendationUseCase marks recommendation accepted")
    void shouldMarkRecommendationAccepted() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);
        when(recommendationRepository.save(any(AgronomicRecommendation.class))).thenAnswer(i -> i.getArgument(0));

        AcceptRecommendationUseCase useCase = new AcceptRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        AgronomicRecommendation accepted = useCase.execute(sampleRecommendation.getId());
        assertThat(accepted.getStatus()).isEqualTo(RecommendationStatus.ACCEPTED);
        assertThat(accepted.getAcceptedBy()).isEqualTo(farmerUserId);
        assertThat(accepted.getAcceptedAt()).isNotNull();
    }

    @Test
    @DisplayName("DismissRecommendationUseCase requires non-blank reason and transitions to DISMISSED")
    void shouldDismissRecommendationWithReason() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);
        when(recommendationRepository.save(any(AgronomicRecommendation.class))).thenAnswer(i -> i.getArgument(0));

        DismissRecommendationUseCase useCase = new DismissRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        AgronomicRecommendation dismissed = useCase.execute(sampleRecommendation.getId(), "Already irrigated via secondary pump");
        assertThat(dismissed.getStatus()).isEqualTo(RecommendationStatus.DISMISSED);
        assertThat(dismissed.getDismissedBy()).isEqualTo(farmerUserId);
        assertThat(dismissed.getDismissalReason()).isEqualTo("Already irrigated via secondary pump");
    }

    @Test
    @DisplayName("CompleteRecommendationUseCase transitions ACCEPTED recommendation to COMPLETED")
    void shouldCompleteAcceptedRecommendation() {
        sampleRecommendation.accept(farmerUserId);

        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);
        when(recommendationRepository.save(any(AgronomicRecommendation.class))).thenAnswer(i -> i.getArgument(0));

        CompleteRecommendationUseCase useCase = new CompleteRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        AgronomicRecommendation completed = useCase.execute(sampleRecommendation.getId());
        assertThat(completed.getStatus()).isEqualTo(RecommendationStatus.COMPLETED);
        assertThat(completed.getCompletedBy()).isEqualTo(farmerUserId);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Dismissal without reason fails with BusinessRuleException")
    void shouldFailDismissalWithoutReason() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(farmerUserId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(recommendationRepository.findById(sampleRecommendation.getId())).thenReturn(Optional.of(sampleRecommendation));
        when(fieldOwnershipChecker.getFieldForRead(testFieldId, farmerUserId, UserRole.FARMER)).thenReturn(testField);

        DismissRecommendationUseCase useCase = new DismissRecommendationUseCase(
                recommendationRepository, fieldOwnershipChecker, currentUserProvider
        );

        assertThatThrownBy(() -> useCase.execute(sampleRecommendation.getId(), "   "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Dismissal reason cannot be blank");
    }
}
