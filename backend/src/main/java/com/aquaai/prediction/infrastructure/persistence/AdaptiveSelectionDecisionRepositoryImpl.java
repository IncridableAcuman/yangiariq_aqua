package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.AdaptiveSelectionDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AdaptiveSelectionDecisionRepositoryImpl implements AdaptiveSelectionDecisionRepository {

    private final SpringDataAdaptiveSelectionDecisionRepository springDataRepo;

    @Override
    public AdaptiveSelectionDecision save(AdaptiveSelectionDecision decision) {
        AdaptiveSelectionDecisionJpaEntity entity = PredictionAdaptiveJpaMapper.toJpa(decision);
        AdaptiveSelectionDecisionJpaEntity saved = springDataRepo.save(entity);
        return PredictionAdaptiveJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<AdaptiveSelectionDecision> findById(UUID id) {
        return springDataRepo.findById(id).map(PredictionAdaptiveJpaMapper::toDomain);
    }

    @Override
    public Optional<AdaptiveSelectionDecision> findByPredictionId(UUID predictionId) {
        return springDataRepo.findByPredictionId(predictionId).map(PredictionAdaptiveJpaMapper::toDomain);
    }

    @Override
    public List<AdaptiveSelectionDecision> findByFieldId(UUID fieldId, int limit) {
        return springDataRepo.findByFieldIdOrderByCreatedAtDesc(fieldId, PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(PredictionAdaptiveJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AdaptiveSelectionDecision> findRecent(int limit) {
        return springDataRepo.findRecentDecisions(PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(PredictionAdaptiveJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AdaptiveSelectionDecision> findByFieldIdAndType(UUID fieldId, PredictionType type, int limit) {
        return springDataRepo.findByFieldIdAndPredictionTypeOrderByCreatedAtDesc(
                        fieldId, type.name(), PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(PredictionAdaptiveJpaMapper::toDomain)
                .toList();
    }

    @Override
    public long countBySelectionMode(ContextMatchLevel mode) {
        return springDataRepo.countBySelectionMode(mode.name());
    }

    @Override
    public long countTotal() {
        return springDataRepo.count();
    }
}
