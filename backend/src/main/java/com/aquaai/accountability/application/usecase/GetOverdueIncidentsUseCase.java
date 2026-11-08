package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.AccountabilityNotificationPort;
import com.aquaai.accountability.domain.port.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOverdueIncidentsUseCase {

    private final IncidentRepository incidentRepository;
    private final AccountabilityNotificationPort notificationPort;

    @Transactional(readOnly = true)
    public List<WaterIncident> execute() {
        Instant now = Instant.now();
        List<WaterIncident> overdue = incidentRepository.findOverdue(now);
        for (WaterIncident incident : overdue) {
            notificationPort.sendSlaBreachAlert(
                    incident.getId(),
                    incident.getIncidentNumber(),
                    incident.getTitle(),
                    "Incident " + incident.getIncidentNumber() + " has breached SLA deadline (" + incident.getSlaDeadline() + ")"
            );
        }
        return overdue;
    }
}
