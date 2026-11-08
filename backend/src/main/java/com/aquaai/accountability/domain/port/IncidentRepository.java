package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.WaterIncident;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {

    WaterIncident save(WaterIncident incident);

    Optional<WaterIncident> findById(UUID id);

    Optional<WaterIncident> findByIncidentNumber(String incidentNumber);

    Optional<WaterIncident> findBySource(String sourceModule, String sourceEntityType, UUID sourceEntityId);

    List<WaterIncident> findAll(
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
    );

    long countTotal(
            IncidentCategory category,
            IncidentSeverity severity,
            IncidentStatus status,
            UUID wuaId,
            UUID canalId,
            UUID fieldId,
            Boolean overdueOnly,
            Instant startDate,
            Instant endDate
    );

    List<WaterIncident> findOverdue(Instant now);

    long countByStatus(IncidentStatus status);

    long countOverdue(Instant now);

    long countBySeverity(IncidentSeverity severity);
}
