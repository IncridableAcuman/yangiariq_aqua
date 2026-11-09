package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PilotParticipant;
import com.aquaai.pilot.domain.repository.PilotParticipantRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPilotParticipantRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PilotParticipantRepositoryImpl implements PilotParticipantRepository {

    private final SpringDataPilotParticipantRepository repository;
    private final PilotEntityMapper mapper;

    public PilotParticipantRepositoryImpl(SpringDataPilotParticipantRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PilotParticipant save(PilotParticipant participant) {
        var entity = mapper.toEntity(participant);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PilotParticipant> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PilotParticipant> findByPilotId(UUID pilotId) {
        return repository.findByPilotId(pilotId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PilotParticipant> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<PilotParticipant> findByPilotIdAndUserId(UUID pilotId, UUID userId) {
        return repository.findByPilotIdAndUserId(pilotId, userId).map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
