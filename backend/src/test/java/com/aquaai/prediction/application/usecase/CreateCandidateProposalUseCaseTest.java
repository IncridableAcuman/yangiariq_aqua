package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.CreateCandidateProposalRequest;
import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceAuditLog;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import com.aquaai.prediction.domain.port.GovernanceAuditLogRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCandidateProposalUseCase Unit Tests")
class CreateCandidateProposalUseCaseTest {

    @Mock
    private CalibrationProposalRepository proposalRepository;

    @Mock
    private PredictionMethodVersionRepository versionRepository;

    @Mock
    private GovernanceAuditLogRepository auditLogRepository;

    private CreateCandidateProposalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCandidateProposalUseCase(
                proposalRepository,
                versionRepository,
                auditLogRepository
        );
    }

    @Test
    @DisplayName("Should successfully create candidate proposal and candidate method version")
    void shouldCreateCandidateProposalSuccessfully() {
        PredictionMethodVersion activeV1 = new PredictionMethodVersion(
                UUID.randomUUID(),
                PredictionType.WATER_DEMAND_FORECAST,
                "WATER_DEMAND_RULE_V1",
                MethodVersionStatus.ACTIVE,
                "Active V1",
                "{\"applicationEfficiency\":0.82,\"leachingFraction\":0.10}",
                true,
                Instant.now(),
                Instant.now(),
                null,
                "SYSTEM",
                "SYSTEM",
                0L
        );

        when(versionRepository.findActiveByPredictionType(PredictionType.WATER_DEMAND_FORECAST))
                .thenReturn(Optional.of(activeV1));
        when(versionRepository.findByVersionTag("WATER_DEMAND_RULE_V2"))
                .thenReturn(Optional.empty());
        when(versionRepository.save(any(PredictionMethodVersion.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(proposalRepository.save(any(CalibrationProposal.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateCandidateProposalRequest request = new CreateCandidateProposalRequest(
                "WATER_DEMAND_FORECAST",
                "Observed +8.5% overprediction in late summer tomato parcels",
                "Adjust application efficiency to 0.85 and leaching fraction to 0.08",
                "{\"applicationEfficiency\":0.85,\"leachingFraction\":0.08}",
                "Anticipated 20% reduction in water demand MAE",
                "LOW"
        );

        CalibrationProposalDto result = useCase.execute(request, "admin-user-1", "ROLE_ADMIN");

        assertThat(result).isNotNull();
        assertThat(result.predictionType()).isEqualTo("WATER_DEMAND_FORECAST");
        assertThat(result.currentVersionTag()).isEqualTo("WATER_DEMAND_RULE_V1");
        assertThat(result.candidateVersionTag()).isEqualTo("WATER_DEMAND_RULE_V2");
        assertThat(result.riskLevel()).isEqualTo("LOW");
        assertThat(result.affectedScope()).isEqualTo("ALL_PARCELS");

        // Verify candidate version was persisted with CANDIDATE status
        ArgumentCaptor<PredictionMethodVersion> versionCaptor = ArgumentCaptor.forClass(PredictionMethodVersion.class);
        verify(versionRepository).save(versionCaptor.capture());
        PredictionMethodVersion savedVersion = versionCaptor.getValue();
        assertThat(savedVersion.getVersionTag()).isEqualTo("WATER_DEMAND_RULE_V2");
        assertThat(savedVersion.getStatus()).isEqualTo(MethodVersionStatus.CANDIDATE);
        assertThat(savedVersion.isImmutable()).isFalse();

        // Verify audit log recorded
        ArgumentCaptor<GovernanceAuditLog> auditCaptor = ArgumentCaptor.forClass(GovernanceAuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());
        GovernanceAuditLog savedLog = auditCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(GovernanceAction.PROPOSAL_CREATED);
        assertThat(savedLog.getActorId()).isEqualTo("admin-user-1");
    }

    @Test
    @DisplayName("Should strictly reject proposal configuration attempting to modify hard hydraulic safety interlocks")
    void shouldRejectHardSafetyParameterModification() {
        CreateCandidateProposalRequest illegalRequest = new CreateCandidateProposalRequest(
                "WATER_DEMAND_FORECAST",
                "Attempting to modify hard hydraulic safety threshold",
                "Override pipe burst pressure",
                "{\"applicationEfficiency\":0.85,\"pipeBurstPressureBar\":8.0}",
                "Unsafe modification",
                "CRITICAL"
        );

        assertThatThrownBy(() -> useCase.execute(illegalRequest, "actor-2", "ROLE_ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hard hydraulic safety parameters cannot be modified");

        verify(proposalRepository, never()).save(any());
        verify(versionRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no active production version exists")
    void shouldThrowWhenNoActiveVersionFound() {
        when(versionRepository.findActiveByPredictionType(PredictionType.SOIL_MOISTURE_FORECAST))
                .thenReturn(Optional.empty());

        CreateCandidateProposalRequest request = new CreateCandidateProposalRequest(
                "SOIL_MOISTURE_FORECAST",
                "Calibrate smoothing alpha",
                "Change alpha to 0.30",
                "{\"smoothingAlpha\":0.30}",
                "Better responsiveness",
                "LOW"
        );

        assertThatThrownBy(() -> useCase.execute(request, "admin-1", "ROLE_ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No active version found");

        verify(proposalRepository, never()).save(any());
    }
}
