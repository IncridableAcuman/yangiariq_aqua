package com.aquaai.analytics.application.usecase;

import com.aquaai.analytics.api.dto.FarmInsightDto;
import com.aquaai.analytics.api.dto.FieldPerformanceDto;
import com.aquaai.analytics.api.dto.FieldPerformanceRankingResponse;
import com.aquaai.analytics.application.service.FarmInsightEngine;
import com.aquaai.analytics.domain.model.InsightCategory;
import com.aquaai.analytics.domain.model.InsightSeverity;
import com.aquaai.analytics.domain.model.PerformancePeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFarmInsightsUseCase {

    private final GetFieldPerformanceRankingUseCase rankingUseCase;
    private final FarmInsightEngine insightEngine;

    @Transactional(readOnly = true)
    public List<FarmInsightDto> execute(
            PerformancePeriodType periodType,
            Instant customFrom,
            Instant customTo,
            UUID targetFieldId,
            InsightCategory categoryFilter,
            InsightSeverity severityFilter
    ) {
        // Retrieve evaluated fields across tenant scope (max size 100)
        FieldPerformanceRankingResponse ranking = rankingUseCase.execute(
                periodType,
                customFrom,
                customTo,
                null,
                null,
                0,
                100,
                "score",
                "asc"
        );

        List<FieldPerformanceDto> fields = ranking.getFields();
        if (targetFieldId != null) {
            fields = fields.stream()
                    .filter(f -> targetFieldId.equals(f.getFieldId()))
                    .toList();
        }

        List<FarmInsightDto> allInsights = insightEngine.generateInsights(fields);

        Stream<FarmInsightDto> stream = allInsights.stream();
        if (categoryFilter != null) {
            stream = stream.filter(i -> i.getCategory() == categoryFilter);
        }
        if (severityFilter != null) {
            stream = stream.filter(i -> i.getSeverity() == severityFilter);
        }

        return stream.toList();
    }
}
