package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.AdaptiveOverviewResponse;
import com.aquaai.prediction.application.dto.AdaptiveSelectionDecisionDto;
import com.aquaai.prediction.application.dto.ContextPerformanceProfileDto;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.port.AdaptiveSelectionDecisionRepository;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAdaptiveOverviewUseCase {

    private final PredictionMethodVersionRepository versionRepository;
    private final AdaptiveSelectionDecisionRepository decisionRepository;
    private final GetContextPerformanceProfilesUseCase profilesUseCase;
    private final GetAdaptiveSelectionHistoryUseCase selectionHistoryUseCase;

    @Transactional(readOnly = true)
    public AdaptiveOverviewResponse execute() {
        List<PredictionMethodVersion> allVersions = versionRepository.findAll();
        int totalAdaptiveMethods = allVersions.size();
        int activeVersionsCount = (int) allVersions.stream()
                .filter(v -> v.getStatus() == MethodVersionStatus.ACTIVE)
                .count();

        long exactCount = decisionRepository.countBySelectionMode(ContextMatchLevel.EXACT_CONTEXT);
        long partialCount = decisionRepository.countBySelectionMode(ContextMatchLevel.PARTIAL_CONTEXT);
        long similarCount = decisionRepository.countBySelectionMode(ContextMatchLevel.SIMILAR_CONTEXT);
        long baselineFallbackCount = decisionRepository.countBySelectionMode(ContextMatchLevel.BASELINE_FALLBACK);
        long insufficientContextCount = decisionRepository.countBySelectionMode(ContextMatchLevel.INSUFFICIENT_DATA);

        long contextAwareSelectionsCount = exactCount + partialCount + similarCount;

        List<ContextPerformanceProfileDto> profiles = profilesUseCase.execute(null, null);
        boolean driftWarningActive = profiles.stream().anyMatch(ContextPerformanceProfileDto::contextDriftDetected);

        List<AdaptiveSelectionDecisionDto> recentSelections = selectionHistoryUseCase.execute(null, 15);

        return AdaptiveOverviewResponse.builder()
                .totalAdaptiveMethodsCount(totalAdaptiveMethods)
                .activeVersionsCount(activeVersionsCount)
                .contextAwareSelectionsCount(contextAwareSelectionsCount)
                .baselineFallbackCount(baselineFallbackCount)
                .insufficientContextCount(insufficientContextCount)
                .contextualDriftWarningActive(driftWarningActive)
                .recentSelections(recentSelections)
                .contextualProfiles(profiles)
                .build();
    }
}
