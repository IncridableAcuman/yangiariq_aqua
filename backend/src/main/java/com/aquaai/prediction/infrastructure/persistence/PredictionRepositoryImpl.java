package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PredictionRepositoryImpl implements PredictionRepository {

    private final SpringDataPredictionRepository springDataRepository;

    @Override
    public void save(Prediction prediction) {
        if (prediction == null) return;
        springDataRepository.save(PredictionJpaMapper.toEntity(prediction));
    }

    @Override
    public void saveAll(Collection<Prediction> predictions) {
        if (predictions == null || predictions.isEmpty()) return;
        List<PredictionJpaEntity> entities = predictions.stream()
                .map(PredictionJpaMapper::toEntity)
                .toList();
        springDataRepository.saveAll(entities);
    }

    @Override
    public Optional<Prediction> findById(UUID id) {
        if (id == null) return Optional.empty();
        return springDataRepository.findById(id).map(PredictionJpaMapper::toDomain);
    }

    @Override
    public Optional<Prediction> findLatestByFieldAndTypeAndHorizon(
            UUID fieldId,
            PredictionType type,
            PredictionHorizon horizon
    ) {
        if (fieldId == null || type == null || horizon == null) return Optional.empty();
        return springDataRepository.findLatestByFieldAndTypeAndHorizon(fieldId, type.name(), horizon.name())
                .map(PredictionJpaMapper::toDomain);
    }

    @Override
    public List<Prediction> findActivePredictionsForField(UUID fieldId, PredictionHorizon horizon) {
        if (fieldId == null || horizon == null) return Collections.emptyList();
        return springDataRepository.findByFieldIdAndHorizon(fieldId, horizon.name()).stream()
                .map(PredictionJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Prediction> findActivePredictionsForFields(
            Collection<UUID> fieldIds,
            PredictionHorizon horizon
    ) {
        if (fieldIds == null || fieldIds.isEmpty() || horizon == null) return Collections.emptyList();
        return springDataRepository.findByFieldIdsAndHorizon(fieldIds, horizon.name()).stream()
                .map(PredictionJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Prediction> findEmergingRisks(
            Collection<UUID> fieldIds,
            PredictionHorizon horizon,
            List<RiskLevel> elevatedRiskLevels
    ) {
        if (fieldIds == null || fieldIds.isEmpty() || horizon == null || elevatedRiskLevels == null || elevatedRiskLevels.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> riskStrings = elevatedRiskLevels.stream().map(Enum::name).toList();
        return springDataRepository.findEmergingRisks(fieldIds, horizon.name(), riskStrings).stream()
                .map(PredictionJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Prediction> findForecastHistory(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to,
            int limit
    ) {
        if (fieldIds == null || fieldIds.isEmpty()) return Collections.emptyList();
        Instant safeFrom = from != null ? from : Instant.EPOCH;
        Instant safeTo = to != null ? to : Instant.now();
        int safeLimit = Math.max(1, Math.min(200, limit));

        return springDataRepository.findForecastHistory(fieldIds, safeFrom, safeTo, PageRequest.of(0, safeLimit)).stream()
                .map(PredictionJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Prediction> findPendingEvaluations(Instant evaluationCutoff) {
        Instant cutoff = evaluationCutoff != null ? evaluationCutoff : Instant.now();
        return springDataRepository.findPendingEvaluations(cutoff).stream()
                .map(PredictionJpaMapper::toDomain)
                .toList();
    }
}
