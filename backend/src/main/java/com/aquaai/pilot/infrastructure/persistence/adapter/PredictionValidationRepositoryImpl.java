package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PredictionEvaluationStatus;
import com.aquaai.pilot.domain.model.PredictionValidationRecord;
import com.aquaai.pilot.domain.repository.PredictionValidationRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPredictionValidationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PredictionValidationRepositoryImpl implements PredictionValidationRepository {

    private final SpringDataPredictionValidationRepository repository;
    private final PilotEntityMapper mapper;

    public PredictionValidationRepositoryImpl(SpringDataPredictionValidationRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PredictionValidationRecord save(PredictionValidationRecord record) {
        var entity = mapper.toEntity(record);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PredictionValidationRecord> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PredictionValidationRecord> findByPilotId(UUID pilotId) {
        return repository.findByPilotIdOrderByEvaluatedAtDesc(pilotId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PredictionValidationRecord> findByFieldId(UUID fieldId) {
        return repository.findByFieldIdOrderByEvaluatedAtDesc(fieldId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PredictionValidationRecord> findByPilotIdAndStatus(UUID pilotId, PredictionEvaluationStatus status) {
        return repository.findByPilotIdAndEvaluationStatusOrderByEvaluatedAtDesc(pilotId, status.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PredictionValidationRecord> findByPilotIdAndModelName(UUID pilotId, String modelName) {
        return repository.findByPilotIdAndModelNameOrderByEvaluatedAtDesc(pilotId, modelName)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByPilotId(UUID pilotId) {
        return repository.countByPilotId(pilotId);
    }
}
