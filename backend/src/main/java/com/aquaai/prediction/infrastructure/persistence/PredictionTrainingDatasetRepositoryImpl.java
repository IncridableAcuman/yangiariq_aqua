package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality;
import com.aquaai.prediction.domain.port.PredictionTrainingDatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PredictionTrainingDatasetRepositoryImpl implements PredictionTrainingDatasetRepository {

    private final SpringDataPredictionTrainingDatasetRepository springDataRepository;

    @Override
    public PredictionTrainingDataset save(PredictionTrainingDataset dataset) {
        PredictionTrainingDatasetJpaEntity entity = toEntity(dataset);
        PredictionTrainingDatasetJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PredictionTrainingDataset> findById(UUID id) {
        if (id == null) return Optional.empty();
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PredictionTrainingDataset> findByPredictionType(PredictionType type) {
        if (type == null) return List.of();
        return springDataRepository.findByPredictionTypeOrderByCreatedAtDesc(type.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<PredictionTrainingDataset> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<PredictionTrainingDataset> findLatestByPredictionType(PredictionType type) {
        if (type == null) return Optional.empty();
        return springDataRepository.findFirstByPredictionTypeOrderByCreatedAtDesc(type.name()).map(this::toDomain);
    }

    @Override
    public Optional<PredictionTrainingDataset> findByDatasetHash(String hash) {
        if (hash == null) return Optional.empty();
        return springDataRepository.findByDatasetHash(hash).map(this::toDomain);
    }

    private PredictionTrainingDatasetJpaEntity toEntity(PredictionTrainingDataset domain) {
        return PredictionTrainingDatasetJpaEntity.builder()
                .id(domain.getId())
                .predictionType(domain.getPredictionType().name())
                .datasetHash(domain.getDatasetHash())
                .featureSchemaVersion(domain.getFeatureSchemaVersion())
                .periodStart(domain.getPeriodStart())
                .periodEnd(domain.getPeriodEnd())
                .fieldScope(domain.getFieldScope())
                .recordCount(domain.getRecordCount())
                .qualityStatus(domain.getQualityStatus().name())
                .qualitySummaryJson(domain.getQualitySummaryJson())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .build();
    }

    private PredictionTrainingDataset toDomain(PredictionTrainingDatasetJpaEntity entity) {
        return new PredictionTrainingDataset(
                entity.getId(),
                PredictionType.valueOf(entity.getPredictionType()),
                entity.getDatasetHash(),
                entity.getFeatureSchemaVersion(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getFieldScope(),
                entity.getRecordCount(),
                TrainingDatasetQuality.DataQualityStatus.valueOf(entity.getQualityStatus()),
                entity.getQualitySummaryJson(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }
}
