package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.AdaptiveSelectionDecisionDto;
import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.port.AdaptiveSelectionDecisionRepository;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAdaptiveSelectionHistoryUseCase {

    private final AdaptiveSelectionDecisionRepository decisionRepository;
    private final PredictionDataProvider dataProvider;

    @Transactional(readOnly = true)
    public List<AdaptiveSelectionDecisionDto> execute(UUID fieldId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<AdaptiveSelectionDecision> decisions = (fieldId != null)
                ? decisionRepository.findByFieldId(fieldId, safeLimit)
                : decisionRepository.findRecent(safeLimit);

        Map<UUID, String> fieldNames = dataProvider.getAccessibleFields(null, true).stream()
                .collect(Collectors.toMap(FieldMetadata::fieldId, FieldMetadata::name, (a, b) -> a));

        return decisions.stream()
                .map(d -> AdaptiveSelectionDecisionDto.fromDomain(d, fieldNames.get(d.getFieldId())))
                .collect(Collectors.toList());
    }
}
