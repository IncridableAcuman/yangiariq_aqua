package com.aquaai.accountability;

import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.service.IncidentSlaCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentSlaCalculatorTest {

    private IncidentSlaCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IncidentSlaCalculator();
    }

    @Test
    @DisplayName("Critical severity should have 4-hour SLA deadline")
    void criticalShouldHaveFourHourDeadline() {
        Instant now = Instant.now();
        Instant deadline = calculator.calculateDeadline(now, IncidentSeverity.CRITICAL);
        assertNotNull(deadline);
        assertEquals(now.plus(Duration.ofHours(4)), deadline);
    }

    @Test
    @DisplayName("High severity should have 24-hour SLA deadline")
    void highShouldHave24HourDeadline() {
        Instant now = Instant.now();
        Instant deadline = calculator.calculateDeadline(now, IncidentSeverity.HIGH);
        assertNotNull(deadline);
        assertEquals(now.plus(Duration.ofHours(24)), deadline);
    }

    @Test
    @DisplayName("Medium severity should have 72-hour SLA deadline")
    void mediumShouldHave72HourDeadline() {
        Instant now = Instant.now();
        Instant deadline = calculator.calculateDeadline(now, IncidentSeverity.MEDIUM);
        assertNotNull(deadline);
        assertEquals(now.plus(Duration.ofHours(72)), deadline);
    }

    @Test
    @DisplayName("Low severity should have 7-day (168-hour) SLA deadline")
    void lowShouldHave7DayDeadline() {
        Instant now = Instant.now();
        Instant deadline = calculator.calculateDeadline(now, IncidentSeverity.LOW);
        assertNotNull(deadline);
        assertEquals(now.plus(Duration.ofHours(168)), deadline);
    }

    @Test
    @DisplayName("Informational severity should have null SLA deadline")
    void informationalShouldHaveNoDeadline() {
        Instant now = Instant.now();
        Instant deadline = calculator.calculateDeadline(now, IncidentSeverity.INFORMATIONAL);
        assertNull(deadline);
    }

    @Test
    @DisplayName("Overdue detection accurately detects breached active incidents")
    void shouldDetectOverdueIncidents() {
        Instant now = Instant.now();
        Instant deadlinePast = now.minus(Duration.ofHours(2));
        Instant deadlineFuture = now.plus(Duration.ofHours(2));

        assertTrue(calculator.isOverdue(deadlinePast, IncidentStatus.INVESTIGATING, now));
        assertFalse(calculator.isOverdue(deadlineFuture, IncidentStatus.INVESTIGATING, now));

        // Terminal statuses should never be marked overdue
        assertFalse(calculator.isOverdue(deadlinePast, IncidentStatus.RESOLVED, now));
        assertFalse(calculator.isOverdue(deadlinePast, IncidentStatus.DISMISSED, now));
        assertFalse(calculator.isOverdue(deadlinePast, IncidentStatus.CLOSED, now));
    }
}
