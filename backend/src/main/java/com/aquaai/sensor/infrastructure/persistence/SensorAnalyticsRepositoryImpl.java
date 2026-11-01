package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.AggregationPolicy;
import com.aquaai.sensor.domain.analytics.DataQualityMetrics;
import com.aquaai.sensor.domain.analytics.SensorAvailabilitySummary;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.sensor.domain.repository.SensorAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure persistence implementation of {@link SensorAnalyticsRepository}.
 * Executes optimized database projections and bucket grouping.
 */
@Repository
@RequiredArgsConstructor
public class SensorAnalyticsRepositoryImpl implements SensorAnalyticsRepository {

    private final SpringDataSensorReadingRepository springDataReadingRepository;
    private final SpringDataSensorDeviceRepository springDataDeviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TelemetryAggregate> aggregateBySensor(
            UUID sensorId,
            Instant from,
            Instant to,
            AggregationInterval interval,
            ReadingQuality qualityFilter
    ) {
        if (sensorId == null || from == null || to == null || interval == null) {
            return Collections.emptyList();
        }

        ReadingQuality effectiveQuality = qualityFilter != null ? qualityFilter : ReadingQuality.VALID;
        List<SensorReadingPointProjection> points = springDataReadingRepository.findReadingsForSensorAnalytics(
                sensorId,
                from,
                to,
                effectiveQuality
        );

        return groupAndAggregatePoints(points, interval, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetryAggregate> aggregateByField(
            UUID fieldId,
            SensorType sensorType,
            Instant from,
            Instant to,
            AggregationInterval interval,
            ReadingQuality qualityFilter
    ) {
        if (fieldId == null || from == null || to == null || interval == null) {
            return Collections.emptyList();
        }

        ReadingQuality effectiveQuality = qualityFilter != null ? qualityFilter : ReadingQuality.VALID;
        List<SensorReadingPointProjection> points = springDataReadingRepository.findReadingsForFieldAnalytics(
                fieldId,
                sensorType,
                from,
                to,
                effectiveQuality
        );

        return groupAndAggregatePoints(points, interval, false);
    }

    @Override
    @Transactional(readOnly = true)
    public DataQualityMetrics calculateDataQuality(
            UUID sensorId,
            UUID fieldId,
            Instant from,
            Instant to
    ) {
        if (from == null || to == null) {
            return DataQualityMetrics.calculate(0, 0, 0, 0, 0, 0);
        }

        List<Object[]> rows = springDataReadingRepository.countByQualityBetween(sensorId, fieldId, from, to);
        long valid = 0;
        long suspect = 0;
        long invalid = 0;

        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2) {
                    ReadingQuality quality = (ReadingQuality) row[0];
                    long count = ((Number) row[1]).longValue();
                    if (quality == ReadingQuality.VALID) {
                        valid = count;
                    } else if (quality == ReadingQuality.SUSPECT) {
                        suspect = count;
                    } else if (quality == ReadingQuality.INVALID) {
                        invalid = count;
                    }
                }
            }
        }

        long total = valid + suspect + invalid;
        return DataQualityMetrics.calculate(total, valid, suspect, invalid, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorAvailabilitySummary calculateAvailability(
            UUID fieldId,
            Duration offlineThreshold
    ) {
        if (fieldId == null) {
            return SensorAvailabilitySummary.empty();
        }

        List<SensorDeviceJpaEntity> devices = springDataDeviceRepository.findByFieldId(fieldId);
        if (devices == null || devices.isEmpty()) {
            return SensorAvailabilitySummary.empty();
        }

        Duration threshold = offlineThreshold != null ? offlineThreshold : Duration.ofMinutes(30);
        Instant now = Instant.now();
        Instant cutoff = now.minus(threshold);

        int total = devices.size();
        int active = 0;
        int inactive = 0;
        int maintenance = 0;
        int offline = 0;
        int communicating = 0;
        int unresponsive = 0;

        for (SensorDeviceJpaEntity device : devices) {
            SensorStatus status = device.getStatus();
            if (status == SensorStatus.ACTIVE) {
                active++;
                Instant lastSeen = device.getLastSeenAt();
                if (lastSeen != null && !lastSeen.isBefore(cutoff)) {
                    communicating++;
                } else {
                    unresponsive++;
                }
            } else if (status == SensorStatus.INACTIVE) {
                inactive++;
            } else if (status == SensorStatus.MAINTENANCE) {
                maintenance++;
            } else if (status == SensorStatus.OFFLINE) {
                offline++;
            }
        }

        return new SensorAvailabilitySummary(
                total,
                active,
                inactive,
                maintenance,
                offline,
                communicating,
                unresponsive
        );
    }

    private List<TelemetryAggregate> groupAndAggregatePoints(
            List<SensorReadingPointProjection> points,
            AggregationInterval interval,
            boolean isSensorSpecific
    ) {
        if (points == null || points.isEmpty()) {
            return Collections.emptyList();
        }

        // Group points by bucket start instant in chronological order
        Map<Instant, List<SensorReadingPointProjection>> buckets = new LinkedHashMap<>();
        for (SensorReadingPointProjection point : points) {
            Instant bucketStart = interval.truncate(point.getMeasuredAt());
            buckets.computeIfAbsent(bucketStart, k -> new ArrayList<>()).add(point);
        }

        List<TelemetryAggregate> aggregates = new ArrayList<>(buckets.size());
        for (Map.Entry<Instant, List<SensorReadingPointProjection>> entry : buckets.entrySet()) {
            Instant bucketStart = entry.getKey();
            Instant bucketEnd = bucketStart.plus(interval.getDuration());
            List<SensorReadingPointProjection> bucketPoints = entry.getValue();

            if (bucketPoints.isEmpty()) {
                continue;
            }

            // Points within the bucket
            SensorReadingPointProjection firstPoint = bucketPoints.get(0);
            SensorReadingPointProjection lastPoint = bucketPoints.get(bucketPoints.size() - 1);

            UUID fieldId = firstPoint.getFieldId();
            UUID sensorId = isSensorSpecific ? firstPoint.getSensorId() : null;
            SensorType sensorType = firstPoint.getType();
            MeasurementUnit unit = firstPoint.getUnit();

            long count = bucketPoints.size();
            BigDecimal min = firstPoint.getValue();
            BigDecimal max = firstPoint.getValue();
            BigDecimal sum = BigDecimal.ZERO;

            for (SensorReadingPointProjection p : bucketPoints) {
                BigDecimal val = p.getValue();
                if (val.compareTo(min) < 0) {
                    min = val;
                }
                if (val.compareTo(max) > 0) {
                    max = val;
                }
                sum = sum.add(val);
            }

            BigDecimal avg = null;
            if (AggregationPolicy.isAverageMeaningful(sensorType)) {
                avg = sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            }

            BigDecimal calculatedSum = null;
            if (AggregationPolicy.isSumMeaningful(sensorType)) {
                calculatedSum = sum;
            }

            aggregates.add(new TelemetryAggregate(
                    fieldId,
                    sensorId,
                    sensorType,
                    interval,
                    bucketStart,
                    bucketEnd,
                    avg,
                    min,
                    max,
                    calculatedSum,
                    firstPoint.getValue(),
                    firstPoint.getMeasuredAt(),
                    lastPoint.getValue(),
                    lastPoint.getMeasuredAt(),
                    count,
                    unit
            ));
        }

        aggregates.sort(Comparator.comparing(TelemetryAggregate::bucketStartTime));
        return Collections.unmodifiableList(aggregates);
    }
}
