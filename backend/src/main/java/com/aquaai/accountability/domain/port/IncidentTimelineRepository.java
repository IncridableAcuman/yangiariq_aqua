package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.IncidentTimelineEntry;

import java.util.List;
import java.util.UUID;

public interface IncidentTimelineRepository {

    IncidentTimelineEntry save(IncidentTimelineEntry entry);

    List<IncidentTimelineEntry> findByIncidentIdOrderByTimestampAsc(UUID incidentId);
}
