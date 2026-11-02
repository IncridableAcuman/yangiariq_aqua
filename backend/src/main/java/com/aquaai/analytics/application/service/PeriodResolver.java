package com.aquaai.analytics.application.service;

import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.shared.domain.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Resolves standard and custom performance analytics temporal windows.
 */
@Component
public class PeriodResolver {

    public record PeriodWindow(
            Instant start,
            Instant end,
            LocalDate startDate,
            LocalDate endDate,
            String label,
            long durationDays
    ) {}

    public PeriodWindow resolve(PerformancePeriodType type, Instant customFrom, Instant customTo) {
        Instant now = Instant.now();
        Instant start;
        Instant end;

        if (type == null) {
            type = PerformancePeriodType.LAST_30_DAYS;
        }

        switch (type) {
            case TODAY -> {
                start = now.truncatedTo(ChronoUnit.DAYS);
                end = now;
            }
            case LAST_7_DAYS -> {
                start = now.minus(7, ChronoUnit.DAYS);
                end = now;
            }
            case LAST_30_DAYS -> {
                start = now.minus(30, ChronoUnit.DAYS);
                end = now;
            }
            case LAST_90_DAYS -> {
                start = now.minus(90, ChronoUnit.DAYS);
                end = now;
            }
            case SEASON -> {
                // Agricultural season: from April 1st of current year (or March)
                LocalDate today = LocalDate.now(ZoneOffset.UTC);
                int year = today.getYear();
                LocalDate seasonStart = today.getMonthValue() >= 4 ? LocalDate.of(year, 4, 1) : LocalDate.of(year, 1, 1);
                start = seasonStart.atStartOfDay(ZoneOffset.UTC).toInstant();
                end = now;
            }
            case CUSTOM -> {
                if (customFrom == null) {
                    start = now.minus(30, ChronoUnit.DAYS);
                } else {
                    start = customFrom;
                }
                if (customTo == null) {
                    end = now;
                } else {
                    end = customTo;
                }
                if (start.isAfter(end)) {
                    throw new BusinessRuleException("Start date must be before or equal to end date", "INVALID_DATE_RANGE");
                }
                long days = Duration.between(start, end).toDays();
                if (days > 365) {
                    throw new BusinessRuleException("Analytics query period cannot exceed 365 days", "DATE_RANGE_TOO_LARGE");
                }
            }
            default -> {
                start = now.minus(30, ChronoUnit.DAYS);
                end = now;
            }
        }

        LocalDate sDate = start.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate eDate = end.atZone(ZoneOffset.UTC).toLocalDate();
        long days = Math.max(1, Duration.between(start, end).toDays());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneOffset.UTC);
        String label = String.format("%s — %s", dtf.format(start), dtf.format(end));

        return new PeriodWindow(start, end, sDate, eDate, label, days);
    }
}
