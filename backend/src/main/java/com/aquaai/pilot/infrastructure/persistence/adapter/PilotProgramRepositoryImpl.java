package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.PilotProgram;
import com.aquaai.pilot.domain.model.PilotStatus;
import com.aquaai.pilot.domain.repository.PilotProgramRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataPilotProgramRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PilotProgramRepositoryImpl implements PilotProgramRepository {

    private final SpringDataPilotProgramRepository repository;
    private final PilotEntityMapper mapper;

    public PilotProgramRepositoryImpl(SpringDataPilotProgramRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PilotProgram save(PilotProgram program) {
        var entity = mapper.toEntity(program);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PilotProgram> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PilotProgram> findByCode(String code) {
        return repository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<PilotProgram> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<PilotProgram> findByStatus(PilotStatus status) {
        return repository.findByStatus(status.name()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }
}
