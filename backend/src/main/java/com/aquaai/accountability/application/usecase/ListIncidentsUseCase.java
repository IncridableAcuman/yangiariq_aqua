package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListIncidentsUseCase {

    private final IncidentRepository incidentRepository;

    public record PagedIncidentsResult(
            List<WaterIncident> incidents,
            long totalElements,
            int page,
            int size
    ) {}

    @Transactional(readOnly = true)
    public PagedIncidentsResult execute(
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
        List<WaterIncident> list = incidentRepository.findAll(
                category, severity, status, wuaId, canalId, fieldId, overdueOnly, startDate, endDate, page, size
        );
        long total = incidentRepository.countTotal(
                category, severity, status, wuaId, canalId, fieldId, overdueOnly, startDate, endDate
        );
        return new PagedIncidentsResult(list, total, page, size);
    }
}
