package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PilotField;
import com.aquaai.pilot.domain.repository.PilotFieldRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPilotFieldRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PilotFieldRepositoryImpl implements PilotFieldRepository {

    private final SpringDataPilotFieldRepository repository;
    private final PilotEntityMapper mapper;

    public PilotFieldRepositoryImpl(SpringDataPilotFieldRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PilotField save(PilotField pilotField) {
        var entity = mapper.toEntity(pilotField);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PilotField> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PilotField> findByPilotId(UUID pilotId) {
        return repository.findByPilotId(pilotId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<PilotField> findByPilotIdAndFieldId(UUID pilotId, UUID fieldId) {
        return repository.findByPilotIdAndFieldId(pilotId, fieldId).map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
