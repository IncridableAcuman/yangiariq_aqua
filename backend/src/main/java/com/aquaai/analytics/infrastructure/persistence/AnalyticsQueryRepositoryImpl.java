package com.aquaai.analytics.infrastructure.persistence;

import com.aquaai.analytics.domain.model.TrendGranularity;
import com.aquaai.analytics.domain.port.AnalyticsQueryPort;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsQueryRepositoryImpl implements AnalyticsQueryPort {

    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, FieldWaterSummaryData> getFieldWaterSummaries(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return waterConsumptionQueryRepository.getFieldWaterSummaries(fieldIds, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<IrrigationPlanStatus, Long>> getPlanStatusCounts(Collection<UUID> fieldIds, LocalDate fromDate, LocalDate toDate) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT p.fieldId, p.status, COUNT(p)
            FROM SmartIrrigationPlanJpaEntity p
            WHERE p.fieldId IN :fieldIds
              AND (cast(:fromDate as date) IS NULL OR p.plannedDate >= :fromDate)
              AND (cast(:toDate as date) IS NULL OR p.plannedDate <= :toDate)
            GROUP BY p.fieldId, p.status
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getResultList();

        Map<UUID, Map<IrrigationPlanStatus, Long>> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new HashMap<>());
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            IrrigationPlanStatus status = (IrrigationPlanStatus) row[1];
            Long count = (Long) row[2];
            result.computeIfAbsent(fieldId, k -> new HashMap<>()).put(status, count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getPlannedVolumes(Collection<UUID> fieldIds, LocalDate fromDate, LocalDate toDate) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT p.fieldId, COALESCE(SUM(p.recommendedVolumeM3), 0)
            FROM SmartIrrigationPlanJpaEntity p
            WHERE p.fieldId IN :fieldIds
              AND (cast(:fromDate as date) IS NULL OR p.plannedDate >= :fromDate)
              AND (cast(:toDate as date) IS NULL OR p.plannedDate <= :toDate)
            GROUP BY p.fieldId
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .getResultList();

        Map<UUID, BigDecimal> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, BigDecimal.ZERO);
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            BigDecimal volume = (BigDecimal) row[1];
            result.put(fieldId, volume != null ? volume : BigDecimal.ZERO);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<IrrigationEventStatus, Long>> getEventStatusCounts(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT e.fieldId, e.status, COUNT(e)
            FROM IrrigationEventJpaEntity e
            WHERE e.fieldId IN :fieldIds
              AND (cast(:from as timestamp) IS NULL OR e.startedAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR e.startedAt <= :to)
            GROUP BY e.fieldId, e.status
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<UUID, Map<IrrigationEventStatus, Long>> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new HashMap<>());
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            IrrigationEventStatus status = (IrrigationEventStatus) row[1];
            Long count = (Long) row[2];
            result.computeIfAbsent(fieldId, k -> new HashMap<>()).put(status, count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getDeliveredVolumes(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT e.fieldId, COALESCE(SUM(e.actualVolumeM3), 0)
            FROM IrrigationEventJpaEntity e
            WHERE e.fieldId IN :fieldIds
              AND e.status = :status
              AND (cast(:from as timestamp) IS NULL OR e.startedAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR e.startedAt <= :to)
            GROUP BY e.fieldId
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("status", IrrigationEventStatus.COMPLETED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<UUID, BigDecimal> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, BigDecimal.ZERO);
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            BigDecimal volume = (BigDecimal) row[1];
            result.put(fieldId, volume != null ? volume : BigDecimal.ZERO);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Integer> getAverageActualDurationsMinutes(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT e.fieldId, e.startedAt, e.endedAt
            FROM IrrigationEventJpaEntity e
            WHERE e.fieldId IN :fieldIds
              AND e.status = :status
              AND e.startedAt IS NOT NULL
              AND e.endedAt IS NOT NULL
              AND (cast(:from as timestamp) IS NULL OR e.startedAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR e.startedAt <= :to)
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("status", IrrigationEventStatus.COMPLETED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<UUID, List<Long>> fieldDurations = new HashMap<>();
        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            Instant start = (Instant) row[1];
            Instant end = (Instant) row[2];
            long minutes = Math.max(0, Duration.between(start, end).toMinutes());
            fieldDurations.computeIfAbsent(fieldId, k -> new ArrayList<>()).add(minutes);
        }

        Map<UUID, Integer> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            List<Long> durations = fieldDurations.get(fieldId);
            if (durations == null || durations.isEmpty()) {
                result.put(fieldId, 0);
            } else {
                long avg = (long) durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
                result.put(fieldId, (int) avg);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<AnomalySeverity, Long>> getAnomalyCounts(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT a.fieldId, a.severity, COUNT(a)
            FROM AnomalyJpaEntity a
            WHERE a.fieldId IN :fieldIds
              AND (cast(:from as timestamp) IS NULL OR a.detectedAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR a.detectedAt <= :to)
            GROUP BY a.fieldId, a.severity
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<UUID, Map<AnomalySeverity, Long>> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new HashMap<>());
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            AnomalySeverity severity = (AnomalySeverity) row[1];
            Long count = (Long) row[2];
            result.computeIfAbsent(fieldId, k -> new HashMap<>()).put(severity, count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<SensorStatus, Integer>> getSensorStatusCounts(Collection<UUID> fieldIds) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT s.fieldId, s.status, COUNT(s)
            FROM SensorDeviceJpaEntity s
            WHERE s.fieldId IN :fieldIds
            GROUP BY s.fieldId, s.status
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .getResultList();

        Map<UUID, Map<SensorStatus, Integer>> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new HashMap<>());
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            SensorStatus status = (SensorStatus) row[1];
            Long count = (Long) row[2];
            result.computeIfAbsent(fieldId, k -> new HashMap<>()).put(status, count.intValue());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Map<ReadingQuality, Long>> getReadingQualityCounts(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String jpql = """
            SELECT r.fieldId, r.quality, COUNT(r)
            FROM SensorReadingJpaEntity r
            WHERE r.fieldId IN :fieldIds
              AND (cast(:from as timestamp) IS NULL OR r.measuredAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR r.measuredAt <= :to)
            GROUP BY r.fieldId, r.quality
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<UUID, Map<ReadingQuality, Long>> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new HashMap<>());
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            ReadingQuality quality = (ReadingQuality) row[1];
            Long count = (Long) row[2];
            result.computeIfAbsent(fieldId, k -> new HashMap<>()).put(quality, count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Long> getVerifiedOperationsCount(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // An operation is verified if completed AND there exists at least one post-operation soil moisture reading
        // on the field within 24 hours of execution, or explicit verification marker in notes.
        String jpql = """
            SELECT e.fieldId, COUNT(e)
            FROM IrrigationEventJpaEntity e
            WHERE e.fieldId IN :fieldIds
              AND e.status = :status
              AND (cast(:from as timestamp) IS NULL OR e.startedAt >= :from)
              AND (cast(:to as timestamp) IS NULL OR e.startedAt <= :to)
              AND (
                e.notes LIKE '%[VERIFIED]%'
                OR EXISTS (
                  SELECT 1 FROM SensorReadingJpaEntity r
                  WHERE r.fieldId = e.fieldId
                    AND r.type = :moistureType
                    AND r.measuredAt >= e.startedAt
                )
              )
            GROUP BY e.fieldId
        """;

        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("status", IrrigationEventStatus.COMPLETED)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("moistureType", SensorType.SOIL_MOISTURE)
                .getResultList();

        Map<UUID, Long> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, 0L);
        }

        for (Object[] row : rows) {
            UUID fieldId = (UUID) row[0];
            Long count = (Long) row[1];
            result.put(fieldId, count != null ? count : 0L);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendBucketRawData> getChronologicalTrendBuckets(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to,
            TrendGranularity granularity
    ) {
        if (fieldIds == null || fieldIds.isEmpty() || from == null || to == null) {
            return Collections.emptyList();
        }

        // Determine bucket boundaries based on granularity
        ChronoUnit stepUnit = switch (granularity) {
            case DAILY -> ChronoUnit.DAYS;
            case WEEKLY -> ChronoUnit.WEEKS;
            case MONTHLY -> ChronoUnit.MONTHS;
        };

        DateTimeFormatter formatter = switch (granularity) {
            case DAILY -> DateTimeFormatter.ofPattern("dd MMM").withZone(ZoneOffset.UTC);
            case WEEKLY -> DateTimeFormatter.ofPattern("'W'w dd MMM").withZone(ZoneOffset.UTC);
            case MONTHLY -> DateTimeFormatter.ofPattern("MMM yyyy").withZone(ZoneOffset.UTC);
        };

        List<TrendBucketRawData> buckets = new ArrayList<>();
        Instant current = from.truncatedTo(ChronoUnit.DAYS);

        // Fetch all events, plans, readings, anomalies within the range once
        List<Object[]> eventRows = entityManager.createQuery("""
            SELECT e.startedAt, COALESCE(e.actualVolumeM3, 0), e.status
            FROM IrrigationEventJpaEntity e
            WHERE e.fieldId IN :fieldIds
              AND e.startedAt >= :from
              AND e.startedAt <= :to
        """, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<Object[]> planRows = entityManager.createQuery("""
            SELECT p.plannedDate, COALESCE(p.recommendedVolumeM3, 0)
            FROM SmartIrrigationPlanJpaEntity p
            WHERE p.fieldId IN :fieldIds
              AND p.plannedDate >= :fromDate
              AND p.plannedDate <= :toDate
        """, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("fromDate", from.atZone(ZoneOffset.UTC).toLocalDate())
                .setParameter("toDate", to.atZone(ZoneOffset.UTC).toLocalDate())
                .getResultList();

        List<Object[]> readingRows = entityManager.createQuery("""
            SELECT r.measuredAt, r.quality
            FROM SensorReadingJpaEntity r
            WHERE r.fieldId IN :fieldIds
              AND r.measuredAt >= :from
              AND r.measuredAt <= :to
        """, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<Object[]> anomalyRows = entityManager.createQuery("""
            SELECT a.detectedAt, a.severity
            FROM AnomalyJpaEntity a
            WHERE a.fieldId IN :fieldIds
              AND a.detectedAt >= :from
              AND a.detectedAt <= :to
        """, Object[].class)
                .setParameter("fieldIds", fieldIds)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        while (!current.isAfter(to)) {
            Instant next = switch (granularity) {
                case DAILY -> current.plus(1, ChronoUnit.DAYS);
                case WEEKLY -> current.plus(7, ChronoUnit.DAYS);
                case MONTHLY -> current.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
            };

            Instant bucketStart = current;
            Instant bucketEnd = next.isAfter(to) ? to : next;
            LocalDate bucketStartDate = bucketStart.atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate bucketEndDate = bucketEnd.atZone(ZoneOffset.UTC).toLocalDate();

            BigDecimal consumedM3 = BigDecimal.ZERO;
            int completedOps = 0;
            for (Object[] row : eventRows) {
                Instant started = (Instant) row[0];
                BigDecimal vol = (BigDecimal) row[1];
                IrrigationEventStatus status = (IrrigationEventStatus) row[2];
                if (started != null && !started.isBefore(bucketStart) && started.isBefore(bucketEnd)) {
                    if (status == IrrigationEventStatus.COMPLETED) {
                        consumedM3 = consumedM3.add(vol);
                        completedOps++;
                    }
                }
            }

            BigDecimal plannedM3 = BigDecimal.ZERO;
            for (Object[] row : planRows) {
                LocalDate plannedDate = (LocalDate) row[0];
                BigDecimal vol = (BigDecimal) row[1];
                if (plannedDate != null && !plannedDate.isBefore(bucketStartDate) && plannedDate.isBefore(bucketEndDate)) {
                    plannedM3 = plannedM3.add(vol);
                }
            }

            long totalReadings = 0;
            long validReadings = 0;
            for (Object[] row : readingRows) {
                Instant rec = (Instant) row[0];
                ReadingQuality qual = (ReadingQuality) row[1];
                if (rec != null && !rec.isBefore(bucketStart) && rec.isBefore(bucketEnd)) {
                    totalReadings++;
                    if (qual == ReadingQuality.VALID) {
                        validReadings++;
                    }
                }
            }

            int safetyCount = 0;
            for (Object[] row : anomalyRows) {
                Instant detected = (Instant) row[0];
                if (detected != null && !detected.isBefore(bucketStart) && detected.isBefore(bucketEnd)) {
                    safetyCount++;
                }
            }

            buckets.add(new TrendBucketRawData(
                    bucketStart,
                    formatter.format(bucketStart),
                    consumedM3,
                    plannedM3,
                    completedOps,
                    totalReadings,
                    validReadings,
                    safetyCount
            ));

            current = next;
        }

        return buckets;
    }
}
