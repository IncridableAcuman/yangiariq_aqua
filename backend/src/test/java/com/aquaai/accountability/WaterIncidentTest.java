package com.aquaai.accountability;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.WaterIncident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterIncidentTest {

    @Test
    @DisplayName("Reviewing incident accepts to OPEN or dismisses to DISMISSED")
    void testIncidentReview() {
        WaterIncident incident = WaterIncident.builder()
                .title("Canal Leak PK14")
                .description("Visual wet spot on embankment")
                .category(IncidentCategory.CONVEYANCE_LOSS)
                .severity(IncidentSeverity.MEDIUM)
                .confidence(EvidenceConfidence.UNVERIFIED)
                .status(IncidentStatus.DETECTED)
                .sourceModule("MANUAL")
                .sourceEntityType("REPORT")
                .build();

        UUID reviewerId = UUID.randomUUID();
        incident.review(reviewerId, true, "Verified legitimate operational discrepancy");
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals(reviewerId, incident.getReviewedBy());

        // Cannot review non-DETECTED incident
        assertThrows(IllegalStateException.class, () ->
                incident.review(reviewerId, false, "Second review"));
    }

    @Test
    @DisplayName("Simulated evidence confidence cannot be directly converted to verified without proof")
    void testEvidenceConfidenceIntegrity() {
        WaterIncident incident = WaterIncident.builder()
                .title("Simulated Over-Delivery")
                .description("Model predicted discrepancy")
                .category(IncidentCategory.WATER_BALANCE_DISCREPANCY)
                .severity(IncidentSeverity.LOW)
                .confidence(EvidenceConfidence.SIMULATED)
                .status(IncidentStatus.OPEN)
                .sourceModule("SIMULATION")
                .sourceEntityType("MODEL")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                incident.recordInvestigation("SEEPAGE", "Details", EvidenceConfidence.VERIFIED));
    }

    @Test
    @DisplayName("Reopening an incident increments recurrence count and tracks history")
    void testIncidentReopening() {
        WaterIncident incident = WaterIncident.builder()
                .title("Sensor Drift")
                .description("Pressure gauge reading drift")
                .category(IncidentCategory.SENSOR_DATA_QUALITY)
                .severity(IncidentSeverity.LOW)
                .confidence(EvidenceConfidence.VERIFIED)
                .status(IncidentStatus.RESOLVED)
                .sourceModule("SENSOR")
                .sourceEntityType("DEVICE")
                .build();

        assertEquals(0, incident.getRecurrenceCount());
        incident.reopen("Sensor recalibration drifted again after 2 weeks");

        assertEquals(IncidentStatus.REOPENED, incident.getStatus());
        assertEquals(1, incident.getRecurrenceCount());
        assertTrue(incident.getResolutionNotes().contains("[REOPENED]"));
    }

    @Test
    @DisplayName("Overdue status evaluates correctly based on deadline and terminal status")
    void testOverdueEvaluation() {
        Instant now = Instant.now();
        WaterIncident incident = WaterIncident.builder()
                .title("Critical Gate Over-Torque")
                .description("Gate jammed under load")
                .category(IncidentCategory.HYDRAULIC_SAFETY)
                .severity(IncidentSeverity.CRITICAL)
                .confidence(EvidenceConfidence.VERIFIED)
                .status(IncidentStatus.INVESTIGATING)
                .sourceModule("NETWORK")
                .sourceEntityType("GATE")
                .slaDeadline(now.minus(1, ChronoUnit.HOURS))
                .build();

        assertTrue(incident.isOverdue(now));

        incident.resolve("Repaired by mechanics");
        assertFalse(incident.isOverdue(now));
    }
}
