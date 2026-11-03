package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.AdaptiveSelectionDecisionDto;
import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.port.AdaptiveSelectionDecisionRepository;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPredictionAdaptiveDecisionUseCase {

    private final AdaptiveSelectionDecisionRepository decisionRepository;
    private final PredictionDataProvider dataProvider;

    @Transactional(readOnly = true)
    public Optional<AdaptiveSelectionDecisionDto> execute(UUID predictionId) {
        return decisionRepository.findByPredictionId(predictionId)
                .map(d -> {
                    String fieldName = dataProvider.getFieldMetadata(d.getFieldId())
                            .map(FieldMetadata::name)
                            .orElse(null);
                    return AdaptiveSelectionDecisionDto.fromDomain(d, fieldName);
                });
    }
}
