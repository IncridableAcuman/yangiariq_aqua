package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.port.IncidentEvidenceRepository;
import com.aquaai.accountability.infrastructure.persistence.mapper.AccountabilityJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IncidentEvidenceRepositoryImpl implements IncidentEvidenceRepository {

    private final SpringDataIncidentEvidenceRepository springDataRepository;
    private final AccountabilityJpaMapper mapper;

    @Override
    public IncidentEvidence save(IncidentEvidence evidence) {
        var entity = mapper.toEntity(evidence);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<IncidentEvidence> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<IncidentEvidence> findByIncidentId(UUID incidentId) {
        return springDataRepository.findByIncidentIdOrderByRecordedAtAsc(incidentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
