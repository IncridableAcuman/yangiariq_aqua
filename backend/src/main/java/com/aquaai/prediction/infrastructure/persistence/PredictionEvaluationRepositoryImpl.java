package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PredictionEvaluationRepositoryImpl implements PredictionEvaluationRepository {

    private final SpringDataPredictionEvaluationRepository springDataRepository;
    private final PredictionEvaluationJpaMapper mapper;

    @Override
    @Transactional
    public void save(PredictionEvaluation evaluation) {
        if (evaluation == null) return;
        springDataRepository.save(mapper.toEntity(evaluation));
    }

    @Override
    @Transactional
    public void saveAll(Collection<PredictionEvaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) return;
        List<PredictionEvaluationJpaEntity> entities = evaluations.stream()
                .map(mapper::toEntity)
                .toList();
        springDataRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PredictionEvaluation> findById(UUID id) {
        if (id == null) return Optional.empty();
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PredictionEvaluation> findByPredictionId(UUID predictionId) {
        if (predictionId == null) return Optional.empty();
        return springDataRepository.findByPredictionId(predictionId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPredictionId(UUID predictionId) {
        if (predictionId == null) return false;
        return springDataRepository.existsByPredictionId(predictionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionEvaluation> findEvaluations(
            Collection<UUID> fieldIds,
            PredictionType type,
            EvaluationStatus status,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        if (fieldIds == null || fieldIds.isEmpty()) return Collections.emptyList();
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;

        return springDataRepository.findFiltered(fieldIds, typeStr, statusStr, from, to, PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countEvaluations(
            Collection<UUID> fieldIds,
            PredictionType type,
            EvaluationStatus status,
            Instant from,
            Instant to
    ) {
        if (fieldIds == null || fieldIds.isEmpty()) return 0;
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        return springDataRepository.countFiltered(fieldIds, typeStr, statusStr, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionEvaluation> findEvaluationsForPeriod(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to
    ) {
        if (fieldIds == null || fieldIds.isEmpty()) return Collections.emptyList();
        Instant safeTo = to != null ? to : Instant.now();
        Instant safeFrom = from != null ? from : safeTo.minus(java.time.Duration.ofDays(30));

        return springDataRepository.findForPeriod(fieldIds, safeFrom, safeTo).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionEvaluation> findEvaluationsByField(
            UUID fieldId,
            Instant from,
            Instant to
    ) {
        if (fieldId == null) return Collections.emptyList();
        Instant safeTo = to != null ? to : Instant.now();
        Instant safeFrom = from != null ? from : safeTo.minus(java.time.Duration.ofDays(30));

        return springDataRepository.findByFieldForPeriod(fieldId, safeFrom, safeTo).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionEvaluation> findByPredictionTypeAndPeriod(
            PredictionType type,
            Instant from,
            Instant to
    ) {
        if (type == null) return Collections.emptyList();
        return springDataRepository.findByPredictionTypeAndPeriod(type.name(), from, to).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
