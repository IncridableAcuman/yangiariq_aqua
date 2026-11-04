package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.TrainingDatasetDto;
import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionTrainingDatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTrainingDatasetsUseCase {

    private final PredictionTrainingDatasetRepository datasetRepository;

    public List<TrainingDatasetDto> execute(PredictionType type) {
        List<PredictionTrainingDataset> list = type != null
                ? datasetRepository.findByPredictionType(type)
                : datasetRepository.findAll();

        return list.stream()
                .map(d -> new TrainingDatasetDto(
                        d.getId(),
                        d.getPredictionType(),
                        d.getDatasetHash(),
                        d.getFeatureSchemaVersion(),
                        d.getPeriodStart(),
                        d.getPeriodEnd(),
                        d.getFieldScope(),
                        d.getRecordCount(),
                        d.getQualityStatus(),
                        d.getCreatedAt(),
                        d.getCreatedBy()
                ))
                .toList();
    }
}
