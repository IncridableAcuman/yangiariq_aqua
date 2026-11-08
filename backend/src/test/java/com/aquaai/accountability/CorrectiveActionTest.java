package com.aquaai.accountability;

import com.aquaai.accountability.domain.model.ActionStatus;
import com.aquaai.accountability.domain.model.CorrectiveAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorrectiveActionTest {

    @Test
    @DisplayName("Action lifecycle progresses through PROPOSED, APPROVED, IN_PROGRESS, COMPLETED")
    void testActionLifecycle() {
        UUID incidentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        UUID completerId = UUID.randomUUID();

        CorrectiveAction action = CorrectiveAction.builder()
                .incidentId(incidentId)
                .actionNumber("ACT-101")
                .title("Calibrate Flume Sensor")
                .description("Conduct sensor calibration")
                .objective("Accuracy within 2%")
                .actionOwnerId(ownerId)
                .expectedOutcome("Accurate readings")
                .status(ActionStatus.PROPOSED)
                .build();

        assertEquals(ActionStatus.PROPOSED, action.getStatus());

        // Cannot start before approval
        assertThrows(IllegalStateException.class, action::start);

        // Approve
        action.approve(approverId, "Approved by chief operator");
        assertEquals(ActionStatus.APPROVED, action.getStatus());
        assertEquals(approverId, action.getApprovedBy());

        // Start
        action.start();
        assertEquals(ActionStatus.IN_PROGRESS, action.getStatus());

        // Complete
        action.complete(completerId, "Calibration finished, flume calibrated within 1.2%");
        assertEquals(ActionStatus.COMPLETED, action.getStatus());
        assertEquals(completerId, action.getCompletedBy());
        assertNotNull(action.getCompletedAt());

        // Completed cannot be cancelled
        assertThrows(IllegalStateException.class, () -> action.cancel("Cancel completed"));
    }
}
