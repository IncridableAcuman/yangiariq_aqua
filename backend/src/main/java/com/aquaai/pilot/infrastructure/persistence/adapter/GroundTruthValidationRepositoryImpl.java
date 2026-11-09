package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.GroundTruthMeasurement;
import com.aquaai.pilot.domain.model.GroundTruthStatus;
import com.aquaai.pilot.domain.repository.GroundTruthValidationRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataGroundTruthValidationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GroundTruthValidationRepositoryImpl implements GroundTruthValidationRepository {

    private final SpringDataGroundTruthValidationRepository repository;
    private final PilotEntityMapper mapper;

    public GroundTruthValidationRepositoryImpl(SpringDataGroundTruthValidationRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GroundTruthMeasurement save(GroundTruthMeasurement measurement) {
        var entity = mapper.toEntity(measurement);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GroundTruthMeasurement> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<GroundTruthMeasurement> findByPilotId(UUID pilotId) {
        return repository.findByPilotIdOrderByMeasuredAtDesc(pilotId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<GroundTruthMeasurement> findByFieldId(UUID fieldId) {
        return repository.findByFieldIdOrderByMeasuredAtDesc(fieldId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<GroundTruthMeasurement> findByPilotIdAndStatus(UUID pilotId, GroundTruthStatus status) {
        return repository.findByPilotIdAndVerificationStatusOrderByMeasuredAtDesc(pilotId, status.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<GroundTruthMeasurement> findByFieldIdAndMeasurementType(UUID fieldId, String measurementType) {
        return repository.findByFieldIdAndMeasurementTypeOrderByMeasuredAtDesc(fieldId, measurementType)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByPilotId(UUID pilotId) {
        return repository.countByPilotId(pilotId);
    }

    @Override
    public long countByPilotIdAndStatus(UUID pilotId, GroundTruthStatus status) {
        return repository.countByPilotIdAndVerificationStatus(pilotId, status.name());
    }
}
