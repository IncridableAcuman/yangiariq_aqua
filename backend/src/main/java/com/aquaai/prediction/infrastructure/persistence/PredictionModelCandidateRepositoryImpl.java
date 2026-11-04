package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.MethodVersionStatus;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionModelCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PredictionModelCandidateRepositoryImpl implements PredictionModelCandidateRepository {

    private final SpringDataPredictionModelCandidateRepository springDataRepository;

    @Override
    public PredictionModelCandidate save(PredictionModelCandidate candidate) {
        PredictionModelCandidateJpaEntity entity = toEntity(candidate);
        PredictionModelCandidateJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PredictionModelCandidate> findById(UUID id) {
        if (id == null) return Optional.empty();
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PredictionModelCandidate> findByVersionTag(String versionTag) {
        if (versionTag == null) return Optional.empty();
        return springDataRepository.findByVersionTag(versionTag).map(this::toDomain);
    }

    @Override
    public List<PredictionModelCandidate> findByPredictionType(PredictionType type) {
        if (type == null) return List.of();
        return springDataRepository.findByPredictionTypeOrderByCreatedAtDesc(type.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<PredictionModelCandidate> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<PredictionModelCandidate> findActiveByPredictionType(PredictionType type) {
        if (type == null) return Optional.empty();
        return springDataRepository.findFirstByPredictionTypeAndStatusOrderByCreatedAtDesc(type.name(), MethodVersionStatus.ACTIVE.name())
                .map(this::toDomain);
    }

    private PredictionModelCandidateJpaEntity toEntity(PredictionModelCandidate domain) {
        return PredictionModelCandidateJpaEntity.builder()
                .id(domain.getId())
                .predictionType(domain.getPredictionType().name())
                .modelType(domain.getModelType())
                .versionTag(domain.getVersionTag())
                .datasetSnapshotId(domain.getDatasetSnapshotId())
                .featureSchemaVersion(domain.getFeatureSchemaVersion())
                .trainingPeriodStart(domain.getTrainingPeriodStart())
                .trainingPeriodEnd(domain.getTrainingPeriodEnd())
                .validationPeriodStart(domain.getValidationPeriodStart())
                .validationPeriodEnd(domain.getValidationPeriodEnd())
                .testPeriodStart(domain.getTestPeriodStart())
                .testPeriodEnd(domain.getTestPeriodEnd())
                .trainingSampleCount(domain.getTrainingSampleCount())
                .validationSampleCount(domain.getValidationSampleCount())
                .testSampleCount(domain.getTestSampleCount())
                .trainingMetricsJson(domain.getTrainingMetricsJson())
                .validationMetricsJson(domain.getValidationMetricsJson())
                .testMetricsJson(domain.getTestMetricsJson())
                .baselineComparisonMetricsJson(domain.getBaselineComparisonMetricsJson())
                .configurationSnapshotJson(domain.getConfigurationSnapshotJson())
                .artifactHash(domain.getArtifactHash())
                .status(domain.getStatus().name())
                .isImmutable(domain.isImmutable())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .optLockVersion(domain.getOptLockVersion() != null ? domain.getOptLockVersion() : 0L)
                .build();
    }

    private PredictionModelCandidate toDomain(PredictionModelCandidateJpaEntity entity) {
        return new PredictionModelCandidate(
                entity.getId(),
                PredictionType.valueOf(entity.getPredictionType()),
                entity.getModelType(),
                entity.getVersionTag(),
                entity.getDatasetSnapshotId(),
                entity.getFeatureSchemaVersion(),
                entity.getTrainingPeriodStart(),
                entity.getTrainingPeriodEnd(),
                entity.getValidationPeriodStart(),
                entity.getValidationPeriodEnd(),
                entity.getTestPeriodStart(),
                entity.getTestPeriodEnd(),
                entity.getTrainingSampleCount(),
                entity.getValidationSampleCount(),
                entity.getTestSampleCount(),
                entity.getTrainingMetricsJson(),
                entity.getValidationMetricsJson(),
                entity.getTestMetricsJson(),
                entity.getBaselineComparisonMetricsJson(),
                entity.getConfigurationSnapshotJson(),
                entity.getArtifactHash(),
                MethodVersionStatus.valueOf(entity.getStatus()),
                entity.isImmutable(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getOptLockVersion()
        );
    }
}
