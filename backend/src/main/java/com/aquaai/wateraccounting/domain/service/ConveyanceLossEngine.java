package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.ConveyanceLossAssessment;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossClassification;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain service for calculating conveyance losses across canal segments.
 * <p>
 * Formulas:
 * LOSS_VOLUME = max(UPSTREAM_VOLUME - DOWNSTREAM_VOLUME, 0)
 * LOSS_PERCENTAGE = (LOSS_VOLUME / UPSTREAM_VOLUME) * 100
 * <p>
 * Classifications:
 * - <= 5%: NO_SIGNIFICANT_DISCREPANCY
 * - 5% - 15%: VERIFIED_LOSS (normal earthen canal infiltration/evaporation)
 * - 15% - 30%: SUSPECTED_LOSS (leakage, structural degradation)
 * - > 30%: UNACCOUNTED_WITHDRAWAL (possible unauthorized intake)
 * - downstream > upstream: MEASUREMENT_DISCREPANCY (sensor mismatch)
 * - upstream <= 0: INSUFFICIENT_DATA
 */
public class ConveyanceLossEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public ConveyanceLossAssessment assessSegmentLoss(
            UUID edgeId,
            String canalName,
            String edgeType,
            String startNodeName,
            String endNodeName,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal upstreamVolumeM3,
            BigDecimal downstreamVolumeM3,
            EvidenceClassification classification,
            String confidence
    ) {
        BigDecimal up = upstreamVolumeM3 != null ? upstreamVolumeM3 : BigDecimal.ZERO;
        BigDecimal down = downstreamVolumeM3 != null ? downstreamVolumeM3 : BigDecimal.ZERO;

        if (up.compareTo(BigDecimal.ZERO) <= 0) {
            return ConveyanceLossAssessment.builder()
                    .edgeId(edgeId)
                    .canalName(canalName)
                    .edgeType(edgeType)
                    .startNodeName(startNodeName)
                    .endNodeName(endNodeName)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .upstreamVolumeM3(BigDecimal.ZERO)
                    .downstreamVolumeM3(down)
                    .lossVolumeM3(BigDecimal.ZERO)
                    .lossPercentage(BigDecimal.ZERO)
                    .classification(ConveyanceLossClassification.INSUFFICIENT_DATA)
                    .evidenceClassification(EvidenceClassification.MISSING)
                    .confidence("LOW")
                    .explanation("No upstream inflow recorded during the accounting period.")
                    .build();
        }

        // Downstream volume higher than upstream volume indicates a calibration or interval discrepancy
        if (down.compareTo(up) > 0) {
            BigDecimal excess = down.subtract(up);
            return ConveyanceLossAssessment.builder()
                    .edgeId(edgeId)
                    .canalName(canalName)
                    .edgeType(edgeType)
                    .startNodeName(startNodeName)
                    .endNodeName(endNodeName)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .upstreamVolumeM3(up.setScale(2, ROUNDING))
                    .downstreamVolumeM3(down.setScale(2, ROUNDING))
                    .lossVolumeM3(BigDecimal.ZERO)
                    .lossPercentage(BigDecimal.ZERO)
                    .classification(ConveyanceLossClassification.MEASUREMENT_DISCREPANCY)
                    .evidenceClassification(classification != null ? classification : EvidenceClassification.CALCULATED)
                    .confidence(confidence != null ? confidence : "LOW")
                    .explanation(String.format("Downstream measured volume exceeds upstream by %.2f m³; sensor calibration or interval alignment required.", excess))
                    .build();
        }

        BigDecimal lossVolume = up.subtract(down).setScale(2, ROUNDING);
        BigDecimal lossPercentage = lossVolume.multiply(new BigDecimal("100.00")).divide(up, 2, ROUNDING);

        ConveyanceLossClassification lossClass;
        String explanation;

        if (lossPercentage.compareTo(new BigDecimal("5.00")) <= 0) {
            lossClass = ConveyanceLossClassification.NO_SIGNIFICANT_DISCREPANCY;
            explanation = String.format("Conveyance efficiency optimal (%.2f%% loss). Within nominal operational margin.", lossPercentage);
        } else if (lossPercentage.compareTo(new BigDecimal("15.00")) <= 0) {
            lossClass = ConveyanceLossClassification.VERIFIED_LOSS;
            explanation = String.format("Standard unlined conveyance loss of %.2f%% (%.2f m³) due to soil percolation and open surface evaporation.", lossPercentage, lossVolume);
        } else if (lossPercentage.compareTo(new BigDecimal("30.00")) <= 0) {
            lossClass = ConveyanceLossClassification.SUSPECTED_LOSS;
            explanation = String.format("Elevated conveyance loss of %.2f%% (%.2f m³). Potential canal bank seepage or gate leakage suspected.", lossPercentage, lossVolume);
        } else {
            lossClass = ConveyanceLossClassification.UNACCOUNTED_WITHDRAWAL;
            explanation = String.format("Severe loss of %.2f%% (%.2f m³) exceeds physical transmission thresholds; unmetered diversion or major levee breach indicated.", lossPercentage, lossVolume);
        }

        return ConveyanceLossAssessment.builder()
                .edgeId(edgeId)
                .canalName(canalName)
                .edgeType(edgeType)
                .startNodeName(startNodeName)
                .endNodeName(endNodeName)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .upstreamVolumeM3(up.setScale(2, ROUNDING))
                .downstreamVolumeM3(down.setScale(2, ROUNDING))
                .lossVolumeM3(lossVolume)
                .lossPercentage(lossPercentage)
                .classification(lossClass)
                .evidenceClassification(classification != null ? classification : EvidenceClassification.CALCULATED)
                .confidence(confidence != null ? confidence : "HIGH")
                .explanation(explanation)
                .build();
    }
}
