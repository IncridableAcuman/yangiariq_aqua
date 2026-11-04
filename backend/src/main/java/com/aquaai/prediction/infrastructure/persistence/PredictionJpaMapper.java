package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionAccuracyStatus;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;

public final class PredictionJpaMapper {

    private PredictionJpaMapper() {}

    public static Prediction toDomain(PredictionJpaEntity entity) {
        if (entity == null) return null;

        return Prediction.builder()
                .id(entity.getId())
                .fieldId(entity.getFieldId())
                .predictionType(PredictionType.valueOf(entity.getPredictionType()))
                .horizon(PredictionHorizon.valueOf(entity.getHorizon()))
                .targetTime(entity.getTargetTime())
                .predictedValue(entity.getPredictedValue())
                .lowerBound(entity.getLowerBound())
                .upperBound(entity.getUpperBound())
                .unit(entity.getUnit())
                .confidencePercent(entity.getConfidencePercent())
                .riskLevel(entity.getRiskLevel() != null ? RiskLevel.valueOf(entity.getRiskLevel()) : RiskLevel.UNKNOWN)
                .dataSufficiency(entity.getDataSufficiency() != null ? DataSufficiency.valueOf(entity.getDataSufficiency()) : DataSufficiency.INSUFFICIENT)
                .likelihood(entity.getLikelihood() != null ? IrrigationLikelihood.valueOf(entity.getLikelihood()) : null)
                .evidence(entity.getEvidence())
                .assumptions(entity.getAssumptions())
                .recommendedAction(entity.getRecommendedAction())
                .actionTargetModule(entity.getActionTargetModule())
                .actualOutcomeValue(entity.getActualOutcomeValue())
                .outcomeRecordedAt(entity.getOutcomeRecordedAt())
                .accuracyStatus(entity.getAccuracyStatus() != null ? PredictionAccuracyStatus.valueOf(entity.getAccuracyStatus()) : PredictionAccuracyStatus.PENDING)
                .generatedAt(entity.getGeneratedAt())
                .methodVersionTag(entity.getMethodVersionTag() != null ? entity.getMethodVersionTag() : Prediction.getDefaultVersionTag(PredictionType.valueOf(entity.getPredictionType())))
                .configurationSnapshot(entity.getConfigurationSnapshot() != null ? entity.getConfigurationSnapshot() : Prediction.getDefaultConfigurationSnapshot(PredictionType.valueOf(entity.getPredictionType())))
                .modelCandidateId(entity.getModelCandidateId())
                .datasetSnapshotId(entity.getDatasetSnapshotId())
                .build();
    }

    public static PredictionJpaEntity toEntity(Prediction domain) {
        if (domain == null) return null;

        return PredictionJpaEntity.builder()
                .id(domain.getId())
                .fieldId(domain.getFieldId())
                .predictionType(domain.getPredictionType().name())
                .horizon(domain.getHorizon().name())
                .targetTime(domain.getTargetTime())
                .predictedValue(domain.getPredictedValue())
                .lowerBound(domain.getLowerBound())
                .upperBound(domain.getUpperBound())
                .unit(domain.getUnit())
                .confidencePercent(domain.getConfidencePercent())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : RiskLevel.UNKNOWN.name())
                .dataSufficiency(domain.getDataSufficiency() != null ? domain.getDataSufficiency().name() : DataSufficiency.INSUFFICIENT.name())
                .likelihood(domain.getLikelihood() != null ? domain.getLikelihood().name() : null)
                .evidence(domain.getEvidence())
                .assumptions(domain.getAssumptions())
                .recommendedAction(domain.getRecommendedAction())
                .actionTargetModule(domain.getActionTargetModule())
                .actualOutcomeValue(domain.getActualOutcomeValue())
                .outcomeRecordedAt(domain.getOutcomeRecordedAt())
                .accuracyStatus(domain.getAccuracyStatus() != null ? domain.getAccuracyStatus().name() : PredictionAccuracyStatus.PENDING.name())
                .generatedAt(domain.getGeneratedAt())
                .methodVersionTag(domain.getMethodVersionTag() != null ? domain.getMethodVersionTag() : Prediction.getDefaultVersionTag(domain.getPredictionType()))
                .configurationSnapshot(domain.getConfigurationSnapshot() != null ? domain.getConfigurationSnapshot() : Prediction.getDefaultConfigurationSnapshot(domain.getPredictionType()))
                .modelCandidateId(domain.getModelCandidateId())
                .datasetSnapshotId(domain.getDatasetSnapshotId())
                .build();
    }
}
