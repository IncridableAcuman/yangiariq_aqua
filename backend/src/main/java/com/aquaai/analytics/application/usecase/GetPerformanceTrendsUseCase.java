package com.aquaai.analytics.application.usecase;

import com.aquaai.analytics.api.dto.PerformanceTrendPointDto;
import com.aquaai.analytics.api.dto.PerformanceTrendsResponse;
import com.aquaai.analytics.application.service.PeriodResolver;
import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.analytics.domain.model.TrendGranularity;
import com.aquaai.analytics.domain.port.AnalyticsQueryPort;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
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
public class GetPerformanceTrendsUseCase {

    private final AnalyticsQueryPort queryPort;
    private final FieldRepository fieldRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public PerformanceTrendsResponse execute(
            PerformancePeriodType periodType,
            Instant customFrom,
            Instant customTo,
            UUID targetFieldId,
            TrendGranularity granularity
    ) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        PeriodResolver.PeriodWindow window = periodResolver.resolve(periodType, customFrom, customTo);

        // Anti-IDOR Field access resolution
        List<UUID> fieldIds;
        if (targetFieldId != null) {
            Field singleField = fieldOwnershipChecker.getFieldForRead(targetFieldId, currentUserId, currentRole);
            fieldIds = List.of(singleField.getId());
        } else if (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR) {
            fieldIds = fieldRepository.findAll().stream().map(Field::getId).toList();
        } else {
            fieldIds = fieldRepository.findByOwnerId(currentUserId).stream().map(Field::getId).toList();
        }

        if (fieldIds.isEmpty()) {
            return PerformanceTrendsResponse.builder()
                    .granularity(granularity != null ? granularity : TrendGranularity.DAILY)
                    .trendPoints(Collections.emptyList())
                    .build();
        }

        // Granularity auto-selection if not specified
        TrendGranularity resolvedGranularity = granularity;
        if (resolvedGranularity == null) {
            if (window.durationDays() <= 31) {
                resolvedGranularity = TrendGranularity.DAILY;
            } else if (window.durationDays() <= 90) {
                resolvedGranularity = TrendGranularity.WEEKLY;
            } else {
                resolvedGranularity = TrendGranularity.MONTHLY;
            }
        }

        List<AnalyticsQueryPort.TrendBucketRawData> rawBuckets = queryPort.getChronologicalTrendBuckets(
                fieldIds,
                window.start(),
                window.end(),
                resolvedGranularity
        );

        List<PerformanceTrendPointDto> points = new ArrayList<>();
        for (AnalyticsQueryPort.TrendBucketRawData b : rawBuckets) {
            BigDecimal util = null;
            if (b.waterPlannedM3() != null && b.waterPlannedM3().compareTo(BigDecimal.ZERO) > 0) {
                util = b.waterConsumedM3()
                        .divide(b.waterPlannedM3(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP);
            }

            BigDecimal telemetryCov = null;
            if (b.totalReadingsCount() > 0) {
                telemetryCov = BigDecimal.valueOf((b.validReadingsCount() * 100.0) / b.totalReadingsCount())
                        .setScale(1, RoundingMode.HALF_UP);
            }

            points.add(PerformanceTrendPointDto.builder()
                    .timestamp(b.bucketTimestamp())
                    .dateLabel(b.dateLabel())
                    .waterConsumedM3(b.waterConsumedM3())
                    .waterPlannedM3(b.waterPlannedM3())
                    .utilizationPercent(util)
                    .completedOperationsCount(b.completedOperationsCount())
                    .telemetryCoveragePercent(telemetryCov)
                    .safetyEventsCount(b.safetyEventsCount())
                    .build());
        }

        return PerformanceTrendsResponse.builder()
                .granularity(resolvedGranularity)
                .trendPoints(points)
                .build();
    }
}
