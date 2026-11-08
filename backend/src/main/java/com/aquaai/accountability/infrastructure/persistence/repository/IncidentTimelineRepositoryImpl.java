package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import com.aquaai.accountability.infrastructure.persistence.mapper.AccountabilityJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IncidentTimelineRepositoryImpl implements IncidentTimelineRepository {

    private final SpringDataIncidentTimelineRepository springDataRepository;
    private final AccountabilityJpaMapper mapper;

    @Override
    public IncidentTimelineEntry save(IncidentTimelineEntry entry) {
        var entity = mapper.toEntity(entry);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<IncidentTimelineEntry> findByIncidentIdOrderByTimestampAsc(UUID incidentId) {
        return springDataRepository.findByIncidentIdOrderByTimestampAsc(incidentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
