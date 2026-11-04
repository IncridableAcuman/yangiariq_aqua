package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.TrainingDatasetDetailResponse;
import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.port.PredictionTrainingDatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTrainingDatasetDetailUseCase {

    private final PredictionTrainingDatasetRepository datasetRepository;

    public Optional<TrainingDatasetDetailResponse> execute(UUID id) {
        if (id == null) return Optional.empty();
        return datasetRepository.findById(id)
                .map(d -> new TrainingDatasetDetailResponse(
                        d.getId(),
                        d.getPredictionType(),
                        d.getDatasetHash(),
                        d.getFeatureSchemaVersion(),
                        d.getPeriodStart(),
                        d.getPeriodEnd(),
                        d.getFieldScope(),
                        d.getRecordCount(),
                        d.getQualityStatus(),
                        d.getQualitySummaryJson(),
                        d.getCreatedAt(),
                        d.getCreatedBy()
                ));
    }
}
