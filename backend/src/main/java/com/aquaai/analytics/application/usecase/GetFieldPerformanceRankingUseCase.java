package com.aquaai.analytics.application.usecase;

import com.aquaai.analytics.api.dto.FieldPerformanceDto;
import com.aquaai.analytics.api.dto.FieldPerformanceRankingResponse;
import com.aquaai.analytics.application.service.PerformanceScoringService;
import com.aquaai.analytics.application.service.PeriodResolver;
import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.analytics.domain.port.AnalyticsQueryPort;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFieldPerformanceRankingUseCase {

    private final AnalyticsQueryPort queryPort;
    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;
    private final PerformanceScoringService scoringService;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public FieldPerformanceRankingResponse execute(
            PerformancePeriodType periodType,
            Instant customFrom,
            Instant customTo,
            String cropTypeFilter,
            FieldPerformanceStatus statusFilter,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        PeriodResolver.PeriodWindow window = periodResolver.resolve(periodType, customFrom, customTo);

        // Tenancy & accessible fields
        List<Field> accessibleFields;
        if (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR) {
            accessibleFields = fieldRepository.findAll();
        } else {
            accessibleFields = fieldRepository.findByOwnerId(currentUserId);
        }

        if (cropTypeFilter != null && !cropTypeFilter.isBlank()) {
            try {
                CropType crop = CropType.valueOf(cropTypeFilter.toUpperCase());
                accessibleFields = accessibleFields.stream()
                        .filter(f -> f.getCropType() == crop)
                        .toList();
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid crop filter
            }
        }

        if (accessibleFields.isEmpty()) {
            return FieldPerformanceRankingResponse.builder()
                    .fields(Collections.emptyList())
                    .totalElements(0)
                    .page(page)
                    .size(size)
                    .totalPages(0)
                    .build();
        }

        List<UUID> fieldIds = accessibleFields.stream().map(Field::getId).toList();

        // Batch queries
        Map<UUID, FieldWaterSummaryData> waterSummaries = queryPort.getFieldWaterSummaries(fieldIds, window.start(), window.end());
        Map<UUID, Map<IrrigationPlanStatus, Long>> planStatusCounts = queryPort.getPlanStatusCounts(fieldIds, window.startDate(), window.endDate());
        Map<UUID, Map<IrrigationEventStatus, Long>> eventStatusCounts = queryPort.getEventStatusCounts(fieldIds, window.start(), window.end());
        Map<UUID, Map<AnomalySeverity, Long>> anomalyCounts = queryPort.getAnomalyCounts(fieldIds, window.start(), window.end());
        Map<UUID, Map<SensorStatus, Integer>> sensorStatusCounts = queryPort.getSensorStatusCounts(fieldIds);
        Map<UUID, Map<ReadingQuality, Long>> readingQualityCounts = queryPort.getReadingQualityCounts(fieldIds, window.start(), window.end());
        Map<UUID, Long> verifiedCounts = queryPort.getVerifiedOperationsCount(fieldIds, window.start(), window.end());

        List<FieldPerformanceDto> evaluatedFields = new ArrayList<>();

        for (Field field : accessibleFields) {
            UUID fid = field.getId();

            // Water Metrics
            FieldWaterSummaryData w = waterSummaries.get(fid);
            BigDecimal allocated = w != null && w.totalAllocatedVolumeM3() != null ? w.totalAllocatedVolumeM3() : BigDecimal.ZERO;
            BigDecimal consumed = w != null && w.totalConsumedVolumeM3() != null ? w.totalConsumedVolumeM3() : BigDecimal.ZERO;
            BigDecimal remaining = allocated.subtract(consumed);
            BigDecimal utilPercent = allocated.compareTo(BigDecimal.ZERO) > 0 ?
                    consumed.divide(allocated, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) :
                    null;

            String waterStatus;
            if (utilPercent == null) {
                waterStatus = "UNKNOWN";
            } else if (utilPercent.compareTo(BigDecimal.valueOf(100.0)) > 0) {
                waterStatus = "OVER_CONSUMED";
            } else if (utilPercent.compareTo(BigDecimal.valueOf(80.0)) > 0) {
                waterStatus = "WARNING";
            } else {
                waterStatus = "NORMAL";
            }

            // Irrigation Metrics
            Map<IrrigationPlanStatus, Long> pCounts = planStatusCounts.getOrDefault(fid, Collections.emptyMap());
            long totalPlans = pCounts.values().stream().mapToLong(Long::longValue).sum();

            Map<IrrigationEventStatus, Long> eCounts = eventStatusCounts.getOrDefault(fid, Collections.emptyMap());
            long completedOps = eCounts.getOrDefault(IrrigationEventStatus.COMPLETED, 0L);

            BigDecimal adherencePercent = totalPlans > 0 ?
                    BigDecimal.valueOf(Math.min(100.0, (completedOps * 100.0) / totalPlans)).setScale(1, RoundingMode.HALF_UP) :
                    null;

            // Telemetry Quality
            Map<SensorStatus, Integer> sCounts = sensorStatusCounts.getOrDefault(fid, Collections.emptyMap());
            int onlineSensors = sCounts.getOrDefault(SensorStatus.ACTIVE, 0);
            int totalSensors = sCounts.values().stream().mapToInt(Integer::intValue).sum();

            String sensorHealth;
            if (totalSensors == 0) {
                sensorHealth = "NO_SENSORS";
            } else if (onlineSensors == totalSensors) {
                sensorHealth = "ONLINE";
            } else if (onlineSensors > 0) {
                sensorHealth = "DEGRADED";
            } else {
                sensorHealth = "OFFLINE";
            }

            Map<ReadingQuality, Long> qCounts = readingQualityCounts.getOrDefault(fid, Collections.emptyMap());
            long validReadings = qCounts.getOrDefault(ReadingQuality.VALID, 0L);
            long totalReadings = qCounts.values().stream().mapToLong(Long::longValue).sum();
            BigDecimal telemetryCoverage = totalReadings > 0 ?
                    BigDecimal.valueOf((validReadings * 100.0) / totalReadings).setScale(1, RoundingMode.HALF_UP) :
                    null;

            // Safety
            Map<AnomalySeverity, Long> aCounts = anomalyCounts.getOrDefault(fid, Collections.emptyMap());
            long criticalAnomalies = aCounts.getOrDefault(AnomalySeverity.CRITICAL, 0L);
            long totalAnomalies = aCounts.values().stream().mapToLong(Long::longValue).sum();

            // Verification
            long verified = verifiedCounts.getOrDefault(fid, 0L);
            BigDecimal verificationCoverage = completedOps > 0 ?
                    BigDecimal.valueOf((verified * 100.0) / completedOps).setScale(1, RoundingMode.HALF_UP) :
                    null;

            // Scoring
            PerformanceScoringService.ScoringResult scoreResult = scoringService.calculateScore(
                    utilPercent,
                    adherencePercent,
                    telemetryCoverage,
                    totalAnomalies,
                    criticalAnomalies,
                    verificationCoverage
            );

            // Status filter check
            if (statusFilter != null && scoreResult.status() != statusFilter) {
                continue;
            }

            evaluatedFields.add(FieldPerformanceDto.builder()
                    .fieldId(field.getId())
                    .fieldName(field.getName())
                    .cropType(field.getCropType() != null ? field.getCropType().name() : "UNKNOWN")
                    .soilType(field.getSoilType() != null ? field.getSoilType().name() : "UNKNOWN")
                    .areaHectares(field.getAreaHectares() != null ? field.getAreaHectares() : BigDecimal.ZERO)
                    .allocatedM3(allocated)
                    .consumedM3(consumed)
                    .remainingM3(remaining)
                    .utilizationPercent(utilPercent)
                    .waterStatus(waterStatus)
                    .plansCount(totalPlans)
                    .completedOperationsCount(completedOps)
                    .planAdherencePercent(adherencePercent)
                    .telemetryCoveragePercent(telemetryCoverage)
                    .sensorHealth(sensorHealth)
                    .safetyEventCount(totalAnomalies)
                    .criticalAnomalyCount(criticalAnomalies)
                    .verificationCoveragePercent(verificationCoverage)
                    .performanceScore(scoreResult.overallScore())
                    .performanceStatus(scoreResult.status())
                    .scoreBreakdown(scoreResult.breakdown())
                    .build());
        }

        // Sorting
        Comparator<FieldPerformanceDto> comparator = resolveComparator(sortBy);
        if ("asc".equalsIgnoreCase(sortDirection)) {
            evaluatedFields.sort(comparator);
        } else {
            evaluatedFields.sort(comparator.reversed());
        }

        // Pagination
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        int totalElements = evaluatedFields.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        int fromIndex = safePage * safeSize;
        List<FieldPerformanceDto> pageContent;
        if (fromIndex >= totalElements) {
            pageContent = Collections.emptyList();
        } else {
            int toIndex = Math.min(fromIndex + safeSize, totalElements);
            pageContent = evaluatedFields.subList(fromIndex, toIndex);
        }

        return FieldPerformanceRankingResponse.builder()
                .fields(pageContent)
                .totalElements(totalElements)
                .page(safePage)
                .size(safeSize)
                .totalPages(totalPages)
                .build();
    }

    private Comparator<FieldPerformanceDto> resolveComparator(String sortBy) {
        if (sortBy == null) {
            sortBy = "score";
        }
        return switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(FieldPerformanceDto::getFieldName, String.CASE_INSENSITIVE_ORDER);
            case "area" -> Comparator.comparing(FieldPerformanceDto::getAreaHectares);
            case "utilization" -> Comparator.comparing(f -> f.getUtilizationPercent() != null ? f.getUtilizationPercent() : BigDecimal.ZERO);
            case "adherence" -> Comparator.comparing(f -> f.getPlanAdherencePercent() != null ? f.getPlanAdherencePercent() : BigDecimal.ZERO);
            case "telemetry" -> Comparator.comparing(f -> f.getTelemetryCoveragePercent() != null ? f.getTelemetryCoveragePercent() : BigDecimal.ZERO);
            case "safety" -> Comparator.comparing(FieldPerformanceDto::getCriticalAnomalyCount);
            default -> Comparator.comparing(FieldPerformanceDto::getPerformanceScore);
        };
    }
}
