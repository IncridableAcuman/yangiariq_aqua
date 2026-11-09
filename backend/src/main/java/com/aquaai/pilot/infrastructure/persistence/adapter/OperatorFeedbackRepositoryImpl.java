package com.aquaai.pilot.infrastructure.persistence.adapter;

import com.aquaai.pilot.domain.model.OperatorFeedback;
import com.aquaai.pilot.domain.model.OperatorFeedbackCategory;
import com.aquaai.pilot.domain.model.OperatorFeedbackStatus;
import com.aquaai.pilot.domain.repository.OperatorFeedbackRepository;
import com.aquaai.pilot.infrastructure.persistence.mapper.PilotEntityMapper;
import com.aquaai.pilot.infrastructure.persistence.repository.SpringDataOperatorFeedbackRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OperatorFeedbackRepositoryImpl implements OperatorFeedbackRepository {

    private final SpringDataOperatorFeedbackRepository repository;
    private final PilotEntityMapper mapper;

    public OperatorFeedbackRepositoryImpl(SpringDataOperatorFeedbackRepository repository, PilotEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OperatorFeedback save(OperatorFeedback feedback) {
        var entity = mapper.toEntity(feedback);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OperatorFeedback> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<OperatorFeedback> findByPilotId(UUID pilotId) {
        return repository.findByPilotIdOrderByCreatedAtDesc(pilotId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OperatorFeedback> findByPilotIdAndStatus(UUID pilotId, OperatorFeedbackStatus status) {
        return repository.findByPilotIdAndStatusOrderByCreatedAtDesc(pilotId, status.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OperatorFeedback> findByPilotIdAndCategory(UUID pilotId, OperatorFeedbackCategory category) {
        return repository.findByPilotIdAndCategoryOrderByCreatedAtDesc(pilotId, category.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByPilotId(UUID pilotId) {
        return repository.countByPilotId(pilotId);
    }

    @Override
    public long countByPilotIdAndStatus(UUID pilotId, OperatorFeedbackStatus status) {
        return repository.countByPilotIdAndStatus(pilotId, status.name());
    }
}
