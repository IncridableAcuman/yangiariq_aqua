package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Use case retrieving aggregated time-series telemetry buckets across sensors on a designated field parcel.
 */
@Service
@RequiredArgsConstructor
public class GetFieldAnalyticsUseCase {

    private final SensorAnalyticsRepository sensorAnalyticsRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record Query(
            UUID fieldId,
            SensorType sensorType,
            Instant from,
            Instant to,
            AggregationInterval interval,
            ReadingQuality quality
    ) {
    }

    @Transactional(readOnly = true)
    public List<TelemetryAggregate> execute(Query query) {
        if (query.fieldId() == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR enforcement: verify read access to underlying field parcel
        fieldOwnershipChecker.getFieldForRead(query.fieldId(), currentUserId, currentRole);

        Instant to = query.to() != null ? query.to() : Instant.now();
        AggregationInterval interval = query.interval() != null ? query.interval() : AggregationInterval.HOURLY;
        Instant from = query.from() != null ? query.from() : to.minus(7, ChronoUnit.DAYS);

        if (from.isAfter(to)) {
            throw new BusinessRuleException(
                    "Start timestamp ('from') must be before or equal to end timestamp ('to')",
                    "INVALID_DATE_RANGE"
            );
        }

        long rangeDays = Duration.between(from, to).toDays();
        if (rangeDays > interval.getMaxAllowedQueryDays()) {
            throw new BusinessRuleException(
                    "Requested range (" + rangeDays + " days) exceeds maximum allowed range of "
                            + interval.getMaxAllowedQueryDays() + " days for " + interval.getCode() + " interval",
                    "QUERY_RANGE_TOO_LARGE"
            );
        }

        return sensorAnalyticsRepository.aggregateByField(
                query.fieldId(),
                query.sensorType(),
                from,
                to,
                interval,
                query.quality()
        );
    }
}
