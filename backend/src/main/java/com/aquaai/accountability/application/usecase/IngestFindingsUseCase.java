package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.CreateIncidentCommand;
import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.AccountabilityWaterAccountingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestFindingsUseCase {

    private final AccountabilityWaterAccountingPort waterAccountingPort;
    private final CreateIncidentUseCase createIncidentUseCase;

    @Transactional
    public List<WaterIncident> execute() {
        List<WaterIncident> createdIncidents = new ArrayList<>();

        // 1. Ingest Water Reconciliation Discrepancies
        List<AccountabilityWaterAccountingPort.DiscrepancyFinding> discrepancies = waterAccountingPort.findPendingDiscrepancies();
        for (var disc : discrepancies) {
            IncidentSeverity severity = IncidentSeverity.MEDIUM;
            if (disc.discrepancyPercent() != null && disc.discrepancyPercent().compareTo(new BigDecimal("15.0")) > 0) {
                severity = IncidentSeverity.HIGH;
            }

            CreateIncidentCommand cmd = new CreateIncidentCommand(
                    "Reconciliation Discrepancy: " + disc.boundaryName(),
                    "Discrepancy detected across " + disc.boundaryType() + " boundary: " + disc.boundaryName() +
                            ". Recorded volume differs by " + disc.discrepancyVolumeM3() + " m³ (" + disc.discrepancyPercent() + "%).",
                    IncidentCategory.WATER_BALANCE_DISCREPANCY,
                    severity,
                    EvidenceConfidence.PARTIALLY_VERIFIED,
                    "WATER_ACCOUNTING",
                    "RECONCILIATION",
                    disc.reconciliationId(),
                    "REC-" + disc.reconciliationId().toString().substring(0, 8),
                    null,
                    null,
                    null,
                    disc.boundaryName() + " (" + disc.boundaryType() + ")",
                    disc.detectedAt() != null ? disc.detectedAt() : Instant.now(),
                    null,
                    "Automated Reconciliation Monitor",
                    "SYSTEM"
            );
            createdIncidents.add(createIncidentUseCase.execute(cmd));
        }

        // 2. Ingest Significant Conveyance Losses
        List<AccountabilityWaterAccountingPort.LossFinding> losses = waterAccountingPort.findSignificantConveyanceLosses();
        for (var loss : losses) {
            IncidentSeverity severity = IncidentSeverity.HIGH;
            CreateIncidentCommand cmd = new CreateIncidentCommand(
                    "High Conveyance Loss in " + loss.canalName(),
                    "Canal segment " + loss.canalName() + " exhibited significant loss of " + loss.lossVolumeM3() +
                            " m³ (" + loss.lossPercentage() + "%). Classification: " + loss.classification(),
                    IncidentCategory.CONVEYANCE_LOSS,
                    severity,
                    EvidenceConfidence.PARTIALLY_VERIFIED,
                    "WATER_ACCOUNTING",
                    "CANAL_LOSS",
                    loss.edgeId(),
                    "LOSS-" + (loss.edgeId() != null ? loss.edgeId().toString().substring(0, 8) : "GLOBAL"),
                    null,
                    loss.edgeId(),
                    null,
                    loss.canalName(),
                    loss.detectedAt() != null ? loss.detectedAt() : Instant.now(),
                    null,
                    "Conveyance Loss Engine",
                    "SYSTEM"
            );
            createdIncidents.add(createIncidentUseCase.execute(cmd));
        }

        return createdIncidents;
    }
}
