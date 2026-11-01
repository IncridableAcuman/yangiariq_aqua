package com.aquaai.sensor.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.DataQualityMetrics;
import com.aquaai.sensor.domain.analytics.FieldAnalyticsSummaryData;
import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import com.aquaai.sensor.domain.repository.SensorReadingRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case assembling an executive operational analytics summary for an agricultural parcel.
 */
@Service
@RequiredArgsConstructor
public class GetFieldAnalyticsSummaryUseCase {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorAnalyticsRepository sensorAnalyticsRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public FieldAnalyticsSummaryData execute(UUID fieldId) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Enforce anti-IDOR field read access
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        Instant now = Instant.now();
        Instant past24h = now.minus(24, ChronoUnit.HOURS);

        // 1. Latest readings by modality
        List<SensorReading> latestList = sensorReadingRepository.findLatestByFieldIdGroupedByType(fieldId);
        Map<SensorType, SensorReading> latestMap = latestList.stream()
                .collect(Collectors.toMap(SensorReading::getType, r -> r, (a, b) -> a));

        // 2. Hardware availability
        SensorAvailabilitySummary availability = sensorAnalyticsRepository.calculateAvailability(
                fieldId,
                Duration.ofMinutes(30)
        );

        // 3. 24h Data quality
        DataQualityMetrics dataQuality = sensorAnalyticsRepository.calculateDataQuality(
                null,
                fieldId,
                past24h,
                now
        );

        // 4. Past 24h baseline aggregate per modality
        Map<SensorType, TelemetryAggregate> past24hAggregates = new HashMap<>();
        for (SensorType type : latestMap.keySet()) {
            List<TelemetryAggregate> dailyAgg = sensorAnalyticsRepository.aggregateByField(
                    fieldId,
                    type,
                    past24h,
                    now,
                    AggregationInterval.DAILY,
                    ReadingQuality.VALID
            );
            if (!dailyAgg.isEmpty()) {
                past24hAggregates.put(type, dailyAgg.get(dailyAgg.size() - 1));
            }
        }

        return new FieldAnalyticsSummaryData(
                fieldId,
                latestMap,
                past24hAggregates,
                availability,
                dataQuality,
                now
        );
    }
}
