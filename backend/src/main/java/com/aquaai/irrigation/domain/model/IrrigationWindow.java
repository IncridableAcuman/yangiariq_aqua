package com.aquaai.irrigation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Value object representing a planned irrigation time window.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class IrrigationWindow {

    private final Instant startAt;
    private final Instant endAt;
    private final boolean nocturnal;

    public IrrigationWindow(Instant startAt, Instant endAt, ZoneId zoneId) {
        this.startAt = Objects.requireNonNull(startAt, "Start time must not be null");
        this.endAt = Objects.requireNonNull(endAt, "End time must not be null");
        if (!startAt.isBefore(endAt)) {
            throw new BusinessRuleException("Window start time must be strictly before end time");
        }
        ZoneId zone = zoneId != null ? zoneId : ZoneId.of("Asia/Tashkent");
        ZonedDateTime zdtStart = startAt.atZone(zone);
        int hour = zdtStart.getHour();
        // Nocturnal is defined as evening/night hours: 21:00 (9 PM) to 06:00 (6 AM)
        this.nocturnal = hour >= 21 || hour < 6;
    }

    public int getDurationMinutes() {
        return (int) Duration.between(startAt, endAt).toMinutes();
    }
}
