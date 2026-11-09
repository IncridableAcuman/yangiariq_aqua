package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PilotReadinessAssessment;
import com.aquaai.pilot.domain.repository.PilotReadinessRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPilotReadinessRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PilotReadinessRepositoryImpl implements PilotReadinessRepository {

    private final SpringDataPilotReadinessRepository repository;
    private final PilotEntityMapper mapper;

    public PilotReadinessRepositoryImpl(SpringDataPilotReadinessRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PilotReadinessAssessment save(PilotReadinessAssessment assessment) {
        var entity = mapper.toEntity(assessment);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PilotReadinessAssessment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PilotReadinessAssessment> findLatestByPilotId(UUID pilotId) {
        return repository.findFirstByPilotIdOrderByAssessedAtDesc(pilotId).map(mapper::toDomain);
    }

    @Override
    public List<PilotReadinessAssessment> findByPilotId(UUID pilotId) {
        return repository.findByPilotIdOrderByAssessedAtDesc(pilotId).stream().map(mapper::toDomain).toList();
    }
}
