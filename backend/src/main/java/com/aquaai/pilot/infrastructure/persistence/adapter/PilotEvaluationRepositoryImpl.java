package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PilotEvaluationSnapshot;
import com.aquaai.pilot.domain.repository.PilotEvaluationRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPilotEvaluationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PilotEvaluationRepositoryImpl implements PilotEvaluationRepository {

    private final SpringDataPilotEvaluationRepository repository;
    private final PilotEntityMapper mapper;

    public PilotEvaluationRepositoryImpl(SpringDataPilotEvaluationRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PilotEvaluationSnapshot save(PilotEvaluationSnapshot snapshot) {
        var entity = mapper.toEntity(snapshot);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PilotEvaluationSnapshot> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PilotEvaluationSnapshot> findLatestByPilotId(UUID pilotId) {
        return repository.findFirstByPilotIdOrderByCreatedAtDesc(pilotId).map(mapper::toDomain);
    }

    @Override
    public List<PilotEvaluationSnapshot> findByPilotId(UUID pilotId) {
        return repository.findByPilotIdOrderByCreatedAtDesc(pilotId).stream().map(mapper::toDomain).toList();
    }
}
