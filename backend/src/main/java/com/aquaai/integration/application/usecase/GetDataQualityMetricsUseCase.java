package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.DataQualitySummaryDto;
import com.aquaai.integration.domain.model.DataQualityStatus;
import com.aquaai.integration.domain.repository.IngestedProvenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetDataQualityMetricsUseCase {

    private final IngestedProvenanceRepository provenanceRepository;

    @Transactional(readOnly = true)
    public DataQualitySummaryDto execute(Integer days) {
        int windowDays = (days != null && days > 0) ? days : 30;
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        Map<DataQualityStatus, Long> counts = provenanceRepository.getQualityMetricsSummary(since);
        long valid = counts.getOrDefault(DataQualityStatus.VALID, 0L);
        long invalid = counts.getOrDefault(DataQualityStatus.INVALID, 0L);
        long duplicate = counts.getOrDefault(DataQualityStatus.DUPLICATE, 0L);
        long stale = counts.getOrDefault(DataQualityStatus.STALE, 0L);
        long outOfRange = counts.getOrDefault(DataQualityStatus.OUT_OF_RANGE, 0L);
        long unverified = counts.getOrDefault(DataQualityStatus.UNVERIFIED, 0L);
        long incomplete = counts.getOrDefault(DataQualityStatus.INCOMPLETE, 0L);

        long total = valid + invalid + duplicate + stale + outOfRange + unverified + incomplete;
        double score = total > 0 ? (double) (valid + unverified) / total * 100.0 : 100.0;

        return new DataQualitySummaryDto(
                total,
                valid,
                invalid + incomplete,
                duplicate,
                stale,
                outOfRange,
                unverified,
                Math.round(score * 10.0) / 10.0
        );
    }
}
