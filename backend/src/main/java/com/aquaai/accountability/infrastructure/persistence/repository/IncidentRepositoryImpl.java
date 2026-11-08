package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.infrastructure.persistence.mapper.AccountabilityJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IncidentRepositoryImpl implements IncidentRepository {

    private final SpringDataWaterIncidentRepository springDataRepository;
    private final AccountabilityJpaMapper mapper;

    private static final Set<IncidentStatus> TERMINAL_STATUSES = Set.of(
            IncidentStatus.RESOLVED,
            IncidentStatus.DISMISSED,
            IncidentStatus.CLOSED
    );

    @Override
    public WaterIncident save(WaterIncident incident) {
        var entity = mapper.toEntity(incident);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WaterIncident> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterIncident> findByIncidentNumber(String incidentNumber) {
        return springDataRepository.findByIncidentNumber(incidentNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<WaterIncident> findBySource(String sourceModule, String sourceEntityType, UUID sourceEntityId) {
        return springDataRepository.findBySourceModuleAndSourceEntityTypeAndSourceEntityId(
                sourceModule, sourceEntityType, sourceEntityId
        ).map(mapper::toDomain);
    }

    @Override
    public List<WaterIncident> findAll(
            IncidentCategory category,
            IncidentSeverity severity,
            IncidentStatus status,
            UUID wuaId,
            UUID canalId,
            UUID fieldId,
            Boolean overdueOnly,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    ) {
        int pageNumber = Math.max(0, page);
        int pageSize = size > 0 ? size : 20;

        boolean hasFilters = category != null || severity != null || status != null
                || wuaId != null || canalId != null || fieldId != null
                || (overdueOnly != null && overdueOnly)
                || startDate != null || endDate != null;

        if (!hasFilters) {
            return springDataRepository.findAllByOrderByDetectionTimestampDesc(PageRequest.of(pageNumber, pageSize))
                    .getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }

        var pageResult = springDataRepository.filterIncidents(
                category,
                severity,
                status,
                wuaId,
                canalId,
                fieldId,
                startDate,
                endDate,
                overdueOnly,
                Instant.now(),
                TERMINAL_STATUSES,
                PageRequest.of(pageNumber, pageSize)
        );
        return pageResult.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countTotal(
            IncidentCategory category,
            IncidentSeverity severity,
            IncidentStatus status,
            UUID wuaId,
            UUID canalId,
            UUID fieldId,
            Boolean overdueOnly,
            Instant startDate,
            Instant endDate
    ) {
        boolean hasFilters = category != null || severity != null || status != null
                || wuaId != null || canalId != null || fieldId != null
                || (overdueOnly != null && overdueOnly)
                || startDate != null || endDate != null;

        if (!hasFilters) {
            return springDataRepository.count();
        }

        var pageResult = springDataRepository.filterIncidents(
                category,
                severity,
                status,
                wuaId,
                canalId,
                fieldId,
                startDate,
                endDate,
                overdueOnly,
                Instant.now(),
                TERMINAL_STATUSES,
                PageRequest.of(0, 1)
        );
        return pageResult.getTotalElements();
    }

    @Override
    public List<WaterIncident> findOverdue(Instant now) {
        return springDataRepository.findOverdue(now, TERMINAL_STATUSES).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(IncidentStatus status) {
        return springDataRepository.countByStatus(status);
    }

    @Override
    public long countOverdue(Instant now) {
        return springDataRepository.countOverdue(now, TERMINAL_STATUSES);
    }

    @Override
    public long countBySeverity(IncidentSeverity severity) {
        return springDataRepository.countBySeverity(severity);
    }
}
