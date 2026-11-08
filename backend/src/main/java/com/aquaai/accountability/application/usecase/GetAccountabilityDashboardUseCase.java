package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.AccountabilityDashboardSummary;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.port.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GetAccountabilityDashboardUseCase {

    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public AccountabilityDashboardSummary execute() {
        Instant now = Instant.now();
        long open = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long detected = incidentRepository.countByStatus(IncidentStatus.DETECTED);
        long underReview = incidentRepository.countByStatus(IncidentStatus.UNDER_REVIEW);
        long investigating = incidentRepository.countByStatus(IncidentStatus.INVESTIGATING);
        long actionProposed = incidentRepository.countByStatus(IncidentStatus.ACTION_PROPOSED);
        long actionApproved = incidentRepository.countByStatus(IncidentStatus.ACTION_APPROVED);
        long actionInProgress = incidentRepository.countByStatus(IncidentStatus.ACTION_IN_PROGRESS);
        long pendingVerification = incidentRepository.countByStatus(IncidentStatus.PENDING_VERIFICATION);
        long resolved = incidentRepository.countByStatus(IncidentStatus.RESOLVED);
        long reopened = incidentRepository.countByStatus(IncidentStatus.REOPENED);
        long dismissed = incidentRepository.countByStatus(IncidentStatus.DISMISSED);
        long closed = incidentRepository.countByStatus(IncidentStatus.CLOSED);

        long critical = incidentRepository.countBySeverity(IncidentSeverity.CRITICAL);
        long overdue = incidentRepository.countOverdue(now);

        long totalOpen = open + detected + underReview;
        long totalInvestigating = investigating + actionProposed + actionApproved + actionInProgress;
        long total = totalOpen + totalInvestigating + pendingVerification + resolved + reopened + dismissed + closed;

        return new AccountabilityDashboardSummary(
                total,
                totalOpen,
                critical,
                totalInvestigating,
                overdue,
                pendingVerification,
                resolved,
                reopened
        );
    }
}
