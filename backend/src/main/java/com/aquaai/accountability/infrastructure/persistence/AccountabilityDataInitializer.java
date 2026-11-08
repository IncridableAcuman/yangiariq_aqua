package com.aquaai.accountability.infrastructure.persistence;

import com.aquaai.accountability.application.dto.AttachEvidenceCommand;
import com.aquaai.accountability.application.dto.CreateIncidentCommand;
import com.aquaai.accountability.application.dto.ProposeActionCommand;
import com.aquaai.accountability.application.usecase.AttachEvidenceUseCase;
import com.aquaai.accountability.application.usecase.CreateIncidentUseCase;
import com.aquaai.accountability.application.usecase.ProposeCorrectiveActionUseCase;
import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class AccountabilityDataInitializer implements CommandLineRunner {

    private final IncidentRepository incidentRepository;
    private final CreateIncidentUseCase createIncidentUseCase;
    private final AttachEvidenceUseCase attachEvidenceUseCase;
    private final ProposeCorrectiveActionUseCase proposeActionUseCase;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final WaterUserAssociationRepository wuaRepository;

    @Override
    public void run(String... args) {
        if (incidentRepository.countTotal(null, null, null, null, null, null, null, null, null) > 0) {
            log.info("[ACCOUNTABILITY SEED] Incidents already present, skipping demo seed.");
            return;
        }

        log.info("[ACCOUNTABILITY SEED] Initializing operational accountability demo incidents for Yangiariq...");

        List<WaterNetworkEdge> edges = edgeRepository.findAll();
        UUID primaryCanalId = edges.isEmpty() ? null : edges.get(0).getId();
        UUID secondaryCanalId = edges.size() > 1 ? edges.get(1).getId() : primaryCanalId;

        List<WaterUserAssociation> wuas = wuaRepository.findAll();
        UUID wua1Id = wuas.isEmpty() ? null : wuas.get(0).getId();
        UUID wua2Id = wuas.size() > 1 ? wuas.get(1).getId() : wua1Id;

        Instant now = Instant.now();

        // 1. Water Balance Discrepancy
        WaterIncident inc1 = createIncidentUseCase.execute(new CreateIncidentCommand(
                "Shovot-Yangiariq Reach 2 Water Balance Discrepancy",
                "Mass balance comparison between PK12 and PK28 gauging flumes showed an unaccounted deficit of 1,240 m³ (-8.4%) over a 48-hour delivery period.",
                IncidentCategory.WATER_BALANCE_DISCREPANCY,
                IncidentSeverity.HIGH,
                EvidenceConfidence.PARTIALLY_VERIFIED,
                "WATER_ACCOUNTING",
                "RECONCILIATION",
                UUID.randomUUID(),
                "REC-SHOVOT-2026-088",
                wua1Id,
                primaryCanalId,
                null,
                "Shovot Magistral Canal PK12+50 to PK28+00",
                now.minus(18, ChronoUnit.HOURS),
                null,
                "District Water Balance Monitor",
                "SYSTEM"
        ));

        attachEvidenceUseCase.execute(new AttachEvidenceCommand(
                inc1.getId(),
                "CANAL_INSPECTION",
                "Acoustic Doppler Velocity Profile PK24",
                "Acoustic cross-sectional velocity profile indicated lower downstream conveyance efficiency compared to upstream rating curve.",
                "/evidence/adcp-shovot-pk24.pdf",
                EvidenceConfidence.VERIFIED,
                false,
                "DOC-2026-ADCP-088",
                null,
                "Hydraulic Inspector Shokirov",
                "OPERATOR"
        ));

        proposeActionUseCase.execute(new ProposeActionCommand(
                inc1.getId(),
                "Inspect Turnout Offtake Siphons & Calibrate Gauge",
                "Conduct physical inspection of turnout gates 4 through 9 along PK20-PK26 and recalibrate PK28 pressure transducer sensor.",
                "Verify turnout seal integrity and ensure flow rating curve accuracy within +/- 2.5%",
                null,
                "District Canal Operations Team A",
                "HIGH",
                now.plus(24, ChronoUnit.HOURS),
                "Restoration of flow balance discrepancy below 3% threshold",
                "On-site joint inspection and re-gauging verification report",
                null,
                "Lead Dispatcher Karimov",
                "OPERATOR"
        ));

        // 2. Conveyance Loss
        WaterIncident inc2 = createIncidentUseCase.execute(new CreateIncidentCommand(
                "Suspected Seepage Loss in Bogʻot Distribution Lateral",
                "Continuous telemetry monitoring detected persistent conveyance loss exceeding 14.2% across unlined canal segment.",
                IncidentCategory.CONVEYANCE_LOSS,
                IncidentSeverity.MEDIUM,
                EvidenceConfidence.PARTIALLY_VERIFIED,
                "WATER_ACCOUNTING",
                "CANAL_LOSS",
                secondaryCanalId,
                "LOSS-BOGOT-041",
                wua2Id,
                secondaryCanalId,
                null,
                "Bogʻot WUA Distribution Lateral 3",
                now.minus(40, ChronoUnit.HOURS),
                null,
                "Conveyance Loss Engine",
                "SYSTEM"
        ));

        attachEvidenceUseCase.execute(new AttachEvidenceCommand(
                inc2.getId(),
                "TELEMETRY_LOG",
                "48-Hour Differential Hydrograph",
                "Inflow/outflow hydrograph difference log across Bogʻot Lateral flumes.",
                "/telemetry/bogot-lat3-diff.csv",
                EvidenceConfidence.PARTIALLY_VERIFIED,
                false,
                "TLM-BOGOT-48H",
                null,
                "Telemetry Ingestion Worker",
                "SYSTEM"
        ));

        // 3. Hydraulic Surge Alert (Critical)
        createIncidentUseCase.execute(new CreateIncidentCommand(
                "Excessive Surge Pressure Risk at Yangiariq Headworks",
                "Transient wave simulation and upstream level sensors indicated surge gradient approaching 92% of maximum freeboard limit.",
                IncidentCategory.HYDRAULIC_SAFETY,
                IncidentSeverity.CRITICAL,
                EvidenceConfidence.VERIFIED,
                "NETWORK",
                "HYDRAULIC_CONSTRAINT",
                UUID.randomUUID(),
                "SURGE-YW-2026-009",
                wua1Id,
                primaryCanalId,
                null,
                "Yangiariq Headworks Control Gate 1",
                now.minus(2, ChronoUnit.HOURS),
                null,
                "Hydraulic Safety Watchdog",
                "SYSTEM"
        ));

        log.info("[ACCOUNTABILITY SEED] Successfully seeded initial accountability incidents.");
    }
}
