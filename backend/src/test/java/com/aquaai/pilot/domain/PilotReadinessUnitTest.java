package com.aquaai.pilot.domain;

import com.aquaai.pilot.domain.model.PilotReadinessAssessment;
import com.aquaai.pilot.domain.model.PilotReadinessCheck;
import com.aquaai.pilot.domain.model.ReadinessCategory;
import com.aquaai.pilot.domain.model.ReadinessResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PilotReadinessUnitTest {

    @Test
    @DisplayName("Should evaluate overall status as BLOCKED when any mandatory check is BLOCKED")
    void shouldEvaluateBlockedWhenMandatoryBlocked() {
        UUID pilotId = UUID.randomUUID();
        UUID assessorId = UUID.randomUUID();

        List<PilotReadinessCheck> checks = List.of(
                new PilotReadinessCheck(ReadinessCategory.INFRASTRUCTURE, "Backend API", ReadinessResult.PASS, "Active", null, true),
                new PilotReadinessCheck(ReadinessCategory.DATA, "Sensors", ReadinessResult.WARNING, "Simulated fallback", null, false),
                new PilotReadinessCheck(ReadinessCategory.OPERATIONAL, "Operators", ReadinessResult.BLOCKED, "Zero operators assigned", null, true),
                new PilotReadinessCheck(ReadinessCategory.GOVERNANCE, "Approval", ReadinessResult.PASS, "Approved", null, true)
        );

        PilotReadinessAssessment assessment = PilotReadinessAssessment.evaluate(pilotId, assessorId, checks);

        assertThat(assessment.getOverallStatus()).isEqualTo(ReadinessResult.BLOCKED);
        assertThat(assessment.getInfrastructureStatus()).isEqualTo(ReadinessResult.PASS);
        assertThat(assessment.getDataStatus()).isEqualTo(ReadinessResult.WARNING);
        assertThat(assessment.getOperationalStatus()).isEqualTo(ReadinessResult.BLOCKED);
        assertThat(assessment.getGovernanceStatus()).isEqualTo(ReadinessResult.PASS);
    }

    @Test
    @DisplayName("Should evaluate overall status as WARNING when non-mandatory warning exists")
    void shouldEvaluateWarningWhenNonMandatoryWarning() {
        UUID pilotId = UUID.randomUUID();
        UUID assessorId = UUID.randomUUID();

        List<PilotReadinessCheck> checks = List.of(
                new PilotReadinessCheck(ReadinessCategory.INFRASTRUCTURE, "Backend API", ReadinessResult.PASS, "Active", null, true),
                new PilotReadinessCheck(ReadinessCategory.DATA, "IoT Hardware", ReadinessResult.WARNING, "Awaiting hardware", null, false),
                new PilotReadinessCheck(ReadinessCategory.OPERATIONAL, "Operators", ReadinessResult.PASS, "Assigned", null, true),
                new PilotReadinessCheck(ReadinessCategory.GOVERNANCE, "Approval", ReadinessResult.PASS, "Approved", null, true)
        );

        PilotReadinessAssessment assessment = PilotReadinessAssessment.evaluate(pilotId, assessorId, checks);

        assertThat(assessment.getOverallStatus()).isEqualTo(ReadinessResult.WARNING);
        assertThat(assessment.getDataStatus()).isEqualTo(ReadinessResult.WARNING);
    }

    @Test
    @DisplayName("Should evaluate overall status as PASS when all checks pass")
    void shouldEvaluatePassWhenAllPass() {
        UUID pilotId = UUID.randomUUID();
        UUID assessorId = UUID.randomUUID();

        List<PilotReadinessCheck> checks = List.of(
                new PilotReadinessCheck(ReadinessCategory.INFRASTRUCTURE, "Backend API", ReadinessResult.PASS, "Active", null, true),
                new PilotReadinessCheck(ReadinessCategory.DATA, "Sources", ReadinessResult.PASS, "Enrolled", null, true),
                new PilotReadinessCheck(ReadinessCategory.OPERATIONAL, "Operators", ReadinessResult.PASS, "Assigned", null, true),
                new PilotReadinessCheck(ReadinessCategory.GOVERNANCE, "Approval", ReadinessResult.PASS, "Approved", null, true)
        );

        PilotReadinessAssessment assessment = PilotReadinessAssessment.evaluate(pilotId, assessorId, checks);

        assertThat(assessment.getOverallStatus()).isEqualTo(ReadinessResult.PASS);
    }
}
