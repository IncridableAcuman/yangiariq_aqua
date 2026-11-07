package com.aquaai.wateraccounting.infrastructure.persistence;

import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import com.aquaai.wateraccounting.domain.model.AccountingDimension;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.LedgerEntryType;
import com.aquaai.wateraccounting.domain.model.ReconciliationStatus;
import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import com.aquaai.wateraccounting.domain.repository.WaterLedgerRepository;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Initializes baseline Yangiariq regional water accounting ledger entries and multi-boundary reconciliations.
 */
@Slf4j
@Component
@Order(100) // Runs after baseline network topology initialization
@RequiredArgsConstructor
public class WaterAccountingDataInitializer implements ApplicationRunner {

    private final WaterLedgerRepository ledgerRepository;
    private final WaterReconciliationRepository reconciliationRepository;
    private final WaterUserAssociationRepository wuaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (ledgerRepository.count() > 0) {
            log.info("Water accounting ledger entries already initialized (count={}). Skipping seeding.", ledgerRepository.count());
            return;
        }

        log.info("Initializing baseline Yangiariq regional water accounting ledger and reconciliations...");

        UUID wua1Id = null;
        UUID wua2Id = null;
        var existingWuas = wuaRepository.findAll();
        if (!existingWuas.isEmpty()) {
            wua1Id = existingWuas.get(0).getId();
            wua2Id = existingWuas.size() > 1 ? existingWuas.get(1).getId() : wua1Id;
        }
        Instant now = Instant.now();

        // 1. Initial Ledger Entries
        List<WaterLedgerEntry> baselineEntries = List.of(
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777701"))
                        .entryType(LedgerEntryType.SOURCE_INFLOW)
                        .accountingBoundary(AccountingDimension.DISTRICT)
                        .volumeM3(new BigDecimal("250000.0000"))
                        .flowRateM3S(new BigDecimal("12.5000"))
                        .sourceNodeOrCanal("Tuyamuyun Bosh Suv Olgich")
                        .destinationNodeOrField("Shavat Bosh Kanali")
                        .wuaId(wua1Id)
                        .eventTimestamp(now.minusSeconds(86400 * 5))
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .dataSource("DISTRICT_INTAKE_ULTRASONIC_GAUGE")
                        .notes("Main intake gravity flow diverted from Amu Darya feeder")
                        .build(),
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777702"))
                        .entryType(LedgerEntryType.CANAL_TRANSFER)
                        .accountingBoundary(AccountingDimension.PRIMARY_CANAL)
                        .volumeM3(new BigDecimal("145000.0000"))
                        .flowRateM3S(new BigDecimal("6.2000"))
                        .sourceNodeOrCanal("Shavat Bosh Kanali")
                        .destinationNodeOrField("Polvon-1 Tarmoq Kanali")
                        .wuaId(wua1Id)
                        .eventTimestamp(now.minusSeconds(86400 * 4))
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .dataSource("SLUICE_HEADGATE_FLOW_LOGGER")
                        .notes("Secondary bifurcation transfer at Polvon headgate")
                        .build(),
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777703"))
                        .entryType(LedgerEntryType.CANAL_TRANSFER)
                        .accountingBoundary(AccountingDimension.PRIMARY_CANAL)
                        .volumeM3(new BigDecimal("85000.0000"))
                        .flowRateM3S(new BigDecimal("4.1000"))
                        .sourceNodeOrCanal("Shavat Bosh Kanali")
                        .destinationNodeOrField("G'ozovot-2 Tarmoq Kanali")
                        .wuaId(wua2Id)
                        .eventTimestamp(now.minusSeconds(86400 * 4))
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .dataSource("SLUICE_HEADGATE_FLOW_LOGGER")
                        .notes("Secondary bifurcation transfer to Bog'ot WUA sector")
                        .build(),
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777704"))
                        .entryType(LedgerEntryType.VERIFIED_LOSS)
                        .accountingBoundary(AccountingDimension.PRIMARY_CANAL)
                        .volumeM3(new BigDecimal("19500.0000"))
                        .flowRateM3S(new BigDecimal("0.7500"))
                        .sourceNodeOrCanal("Shavat Bosh Kanali")
                        .destinationNodeOrField("Unlined Infiltration / Evaporation")
                        .wuaId(wua1Id)
                        .eventTimestamp(now.minusSeconds(86400 * 3))
                        .evidenceClassification(EvidenceClassification.CALCULATED)
                        .dataSource("DIFFERENTIAL_BALANCE_AUDIT")
                        .notes("Calculated open conveyance transmission losses (7.80%)")
                        .build(),
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777705"))
                        .entryType(LedgerEntryType.TURNOUT_DELIVERY)
                        .accountingBoundary(AccountingDimension.WUA)
                        .volumeM3(new BigDecimal("7850.0000"))
                        .flowRateM3S(new BigDecimal("0.0450"))
                        .sourceNodeOrCanal("Polvon-1 Tarmoq Kanali")
                        .destinationNodeOrField("Pakhta Maydoni #1")
                        .wuaId(wua1Id)
                        .eventTimestamp(now.minusSeconds(86400 * 2))
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .dataSource("OPERATOR_ACTUAL_DELIVERY_CONFIRMATION")
                        .notes("Turnout discharge delivered to field intake")
                        .build(),
                WaterLedgerEntry.builder()
                        .id(UUID.fromString("33333333-4444-5555-6666-777777777706"))
                        .entryType(LedgerEntryType.FIELD_CONSUMPTION)
                        .accountingBoundary(AccountingDimension.FIELD)
                        .volumeM3(new BigDecimal("6950.0000"))
                        .flowRateM3S(new BigDecimal("0.0400"))
                        .sourceNodeOrCanal("Pakhta Maydoni #1 Turnout")
                        .destinationNodeOrField("Crop Evapotranspiration / Soil Rootzone")
                        .wuaId(wua1Id)
                        .eventTimestamp(now.minusSeconds(86400))
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .dataSource("COMPLETED_IRRIGATION_EVENT")
                        .notes("Net applied irrigation consumption on field parcel")
                        .build()
        );
        ledgerRepository.saveAll(baselineEntries);

        // 2. Initial Multi-Boundary Reconciliations
        List<WaterReconciliationItem> baselineReconciliations = List.of(
                WaterReconciliationItem.builder()
                        .id(UUID.fromString("44444444-5555-6666-7777-888888888801"))
                        .boundaryType("DISTRICT")
                        .boundaryName("District Intake vs Primary Conveyance")
                        .periodStart(now.minusSeconds(86400 * 30))
                        .periodEnd(now)
                        .expectedVolumeM3(new BigDecimal("250000.0000"))
                        .recordedVolumeM3(new BigDecimal("249500.0000"))
                        .discrepancyVolumeM3(new BigDecimal("-500.0000"))
                        .discrepancyPercent(new BigDecimal("0.20"))
                        .tolerancePercent(new BigDecimal("5.00"))
                        .status(ReconciliationStatus.RECONCILED)
                        .evidenceClassification(EvidenceClassification.CALCULATED)
                        .explanation("Variance of 0.20% is well within allowable 5.00% district intake tolerance.")
                        .build(),
                WaterReconciliationItem.builder()
                        .id(UUID.fromString("44444444-5555-6666-7777-888888888802"))
                        .boundaryType("PRIMARY_TO_SECONDARY")
                        .boundaryName("Shavat Main vs Secondary Branches")
                        .periodStart(now.minusSeconds(86400 * 30))
                        .periodEnd(now)
                        .expectedVolumeM3(new BigDecimal("249500.0000"))
                        .recordedVolumeM3(new BigDecimal("230000.0000"))
                        .discrepancyVolumeM3(new BigDecimal("-19500.0000"))
                        .discrepancyPercent(new BigDecimal("7.82"))
                        .tolerancePercent(new BigDecimal("8.00"))
                        .status(ReconciliationStatus.WITHIN_TOLERANCE)
                        .evidenceClassification(EvidenceClassification.CALCULATED)
                        .explanation("7.82% loss accounted for by transmission seepage and evaporation across 18.5 km reach.")
                        .build(),
                WaterReconciliationItem.builder()
                        .id(UUID.fromString("44444444-5555-6666-7777-888888888803"))
                        .boundaryType("DISPATCH_VS_ACTUAL")
                        .boundaryName("Approved Dispatch vs Confirmed Turnout Delivery")
                        .periodStart(now.minusSeconds(86400 * 30))
                        .periodEnd(now)
                        .expectedVolumeM3(new BigDecimal("12500.0000"))
                        .recordedVolumeM3(new BigDecimal("11950.0000"))
                        .discrepancyVolumeM3(new BigDecimal("-550.0000"))
                        .discrepancyPercent(new BigDecimal("4.40"))
                        .tolerancePercent(new BigDecimal("5.00"))
                        .status(ReconciliationStatus.WITHIN_TOLERANCE)
                        .evidenceClassification(EvidenceClassification.OBSERVED)
                        .explanation("Turnout delivery completed within 4.40% of approved dispatch proposal.")
                        .build(),
                WaterReconciliationItem.builder()
                        .id(UUID.fromString("44444444-5555-6666-7777-888888888804"))
                        .boundaryType("DELIVERY_VS_CONSUMPTION")
                        .boundaryName("Turnout Delivery vs Soil Consumption")
                        .periodStart(now.minusSeconds(86400 * 30))
                        .periodEnd(now)
                        .expectedVolumeM3(new BigDecimal("7850.0000"))
                        .recordedVolumeM3(new BigDecimal("6950.0000"))
                        .discrepancyVolumeM3(new BigDecimal("-900.0000"))
                        .discrepancyPercent(new BigDecimal("11.46"))
                        .tolerancePercent(new BigDecimal("10.00"))
                        .status(ReconciliationStatus.DISCREPANCY)
                        .evidenceClassification(EvidenceClassification.CALCULATED)
                        .explanation("Discrepancy of 11.46% indicates on-farm ditch transmission loss or minor tailwater spill.")
                        .build()
        );
        reconciliationRepository.saveAll(baselineReconciliations);

        log.info("Successfully initialized {} water ledger entries and {} reconciliations.",
                baselineEntries.size(), baselineReconciliations.size());
    }
}
