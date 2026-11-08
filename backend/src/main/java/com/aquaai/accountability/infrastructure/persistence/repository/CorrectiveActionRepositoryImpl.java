package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.port.CorrectiveActionRepository;
import com.aquaai.accountability.infrastructure.persistence.mapper.AccountabilityJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CorrectiveActionRepositoryImpl implements CorrectiveActionRepository {

    private final SpringDataCorrectiveActionRepository springDataRepository;
    private final AccountabilityJpaMapper mapper;

    @Override
    public CorrectiveAction save(CorrectiveAction action) {
        var entity = mapper.toEntity(action);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CorrectiveAction> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CorrectiveAction> findByIncidentId(UUID incidentId) {
        return springDataRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
