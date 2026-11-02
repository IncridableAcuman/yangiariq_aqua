package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.GovernanceAction;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.ProposalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ControlledRolloutManager Domain Service Unit Tests")
class ControlledRolloutManagerUnitTest {

    private ControlledRolloutManager rolloutManager;

    @BeforeEach
    void setUp() {
        rolloutManager = new ControlledRolloutManager();
    }

    @Test
    @DisplayName("Should prevent activation if proposal has not been approved by a human administrator")
    void shouldPreventActivationWithoutHumanApproval() {
        PredictionMethodVersion candidate = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V2",
                "Calibrated candidate",
                "{\"smoothingAlpha\":0.25}",
                "SYSTEM"
        );

        PredictionMethodVersion active = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "Current baseline",
                "{\"smoothingAlpha\":0.30}",
                "SYSTEM"
        );
        active.activate("ADMIN");

        CalibrationProposal proposal = CalibrationProposal.create(
                "PROP-001",
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                "MBE bias detected",
                "MAE = 4.2",
                "Adjust alpha to 0.25",
                "{\"smoothingAlpha\":0.25}",
                "Reduce bias by 20%",
                GovernanceRiskLevel.MEDIUM,
                "SYSTEM"
        );

        // Status is DRAFT or REVIEW_REQUIRED, not APPROVED
        assertThatThrownBy(() -> rolloutManager.activateCandidate(
                candidate, active, proposal, MethodVersionStatus.ACTIVE, "ADMIN", "ADMIN", "Activate"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot activate a proposal that has not been approved");
    }

    @Test
    @DisplayName("Should activate candidate into production and deprecate previous active version")
    void shouldActivateCandidateAndDeprecatePrevious() {
        PredictionMethodVersion candidate = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V2",
                "Calibrated candidate",
                "{\"smoothingAlpha\":0.25}",
                "SYSTEM"
        );

        PredictionMethodVersion active = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "Current baseline",
                "{\"smoothingAlpha\":0.30}",
                "SYSTEM"
        );
        active.activate("ADMIN");

        CalibrationProposal proposal = CalibrationProposal.create(
                "PROP-001",
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                "MBE bias detected",
                "MAE = 4.2",
                "Adjust alpha to 0.25",
                "{\"smoothingAlpha\":0.25}",
                "Reduce bias by 20%",
                GovernanceRiskLevel.MEDIUM,
                "SYSTEM"
        );
        proposal.startBacktest();
        proposal.completeBacktest();
        proposal.approve("ADMIN_USER", "Backtest passed with 15% MAE improvement");

        ControlledRolloutManager.RolloutTransitionResult result = rolloutManager.activateCandidate(
                candidate,
                active,
                proposal,
                MethodVersionStatus.ACTIVE,
                "ADMIN_USER",
                "ROLE_ADMIN",
                "Production activation authorized"
        );

        assertThat(result.updatedCandidate().getStatus()).isEqualTo(MethodVersionStatus.ACTIVE);
        assertThat(result.updatedCandidate().isImmutable()).isTrue();
        assertThat(result.retiredPrevious().getStatus()).isEqualTo(MethodVersionStatus.RETIRED);
        assertThat(result.updatedProposal().getStatus()).isEqualTo(ProposalStatus.ACTIVE);
        assertThat(result.auditLog()).isNotNull();
        assertThat(result.auditLog().getAction()).isEqualTo(GovernanceAction.VERSION_ACTIVATED);
    }

    @Test
    @DisplayName("Should support SHADOW mode rollout without retiring current active version")
    void shouldSupportShadowRollout() {
        PredictionMethodVersion candidate = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V2",
                "Calibrated candidate",
                "{\"smoothingAlpha\":0.25}",
                "SYSTEM"
        );

        PredictionMethodVersion active = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "Current baseline",
                "{\"smoothingAlpha\":0.30}",
                "SYSTEM"
        );
        active.activate("ADMIN");

        CalibrationProposal proposal = CalibrationProposal.create(
                "PROP-001",
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                "MBE bias detected",
                "MAE = 4.2",
                "Adjust alpha to 0.25",
                "{\"smoothingAlpha\":0.25}",
                "Reduce bias by 20%",
                GovernanceRiskLevel.LOW,
                "SYSTEM"
        );
        proposal.startBacktest();
        proposal.completeBacktest();
        proposal.approve("ADMIN_USER", "Approve for shadow testing");

        ControlledRolloutManager.RolloutTransitionResult result = rolloutManager.activateCandidate(
                candidate,
                active,
                proposal,
                MethodVersionStatus.SHADOW,
                "ADMIN_USER",
                "ROLE_ADMIN",
                "Shadow testing launched"
        );

        assertThat(result.updatedCandidate().getStatus()).isEqualTo(MethodVersionStatus.SHADOW);
        assertThat(result.retiredPrevious().getStatus()).isEqualTo(MethodVersionStatus.ACTIVE); // active is NOT retired
        assertThat(result.auditLog().getAction()).isEqualTo(GovernanceAction.VERSION_SHADOW_STARTED);
    }

    @Test
    @DisplayName("Should execute 1-click rollback restoring prior version and marking proposal rolled back")
    void shouldExecuteRollbackSafely() {
        PredictionMethodVersion candidate = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V2",
                "Calibrated candidate",
                "{\"smoothingAlpha\":0.25}",
                "SYSTEM"
        );
        candidate.activate("ADMIN");

        PredictionMethodVersion prior = PredictionMethodVersion.createCandidate(
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "Original baseline",
                "{\"smoothingAlpha\":0.30}",
                "SYSTEM"
        );
        prior.retire("ADMIN");

        CalibrationProposal proposal = CalibrationProposal.create(
                "PROP-001",
                PredictionType.SOIL_MOISTURE_FORECAST,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                "MBE bias", "MAE = 4.2", "Adjust alpha", "{}", "Expected improvement",
                GovernanceRiskLevel.MEDIUM, "SYSTEM"
        );
        proposal.startBacktest();
        proposal.completeBacktest();
        proposal.approve("ADMIN", "Approved");
        proposal.markActive();

        ControlledRolloutManager.RolloutTransitionResult rollbackResult = rolloutManager.executeRollback(
                candidate,
                prior,
                proposal,
                "ADMIN_USER",
                "ROLE_ADMIN",
                "Unforeseen negative bias observed in field parcel 4B"
        );

        assertThat(candidate.getStatus()).isEqualTo(MethodVersionStatus.RETIRED);
        assertThat(prior.getStatus()).isEqualTo(MethodVersionStatus.ACTIVE);
        assertThat(proposal.getStatus()).isEqualTo(ProposalStatus.ROLLED_BACK);
        assertThat(rollbackResult.auditLog().getAction()).isEqualTo(GovernanceAction.VERSION_ROLLBACK);
        assertThat(rollbackResult.auditLog().getReason()).contains("Unforeseen negative bias observed");
    }
}
