package com.aquaai.wateraccounting.application.service;

import com.aquaai.wateraccounting.api.dto.AllocationComplianceResponse;
import com.aquaai.wateraccounting.api.dto.ConservationOpportunityResponse;
import com.aquaai.wateraccounting.api.dto.ConveyanceLossResponse;
import com.aquaai.wateraccounting.api.dto.DistributionEquityResponse;
import com.aquaai.wateraccounting.api.dto.RegionalWaterBalanceResponse;
import com.aquaai.wateraccounting.api.dto.WaterLedgerEntryResponse;
import com.aquaai.wateraccounting.api.dto.WaterReconciliationResponse;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceItem;
import com.aquaai.wateraccounting.domain.model.ConservationOpportunity;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossAssessment;
import com.aquaai.wateraccounting.domain.model.DistributionEquityAssessment;
import com.aquaai.wateraccounting.domain.model.RegionalWaterBalance;
import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;
import com.aquaai.wateraccounting.domain.model.WaterReconciliationItem;
import org.springframework.stereotype.Component;

@Component
public class WaterAccountingDtoMapper {

    public RegionalWaterBalanceResponse toBalanceResponse(RegionalWaterBalance b) {
        if (b == null) return null;
        return new RegionalWaterBalanceResponse(
                b.getBoundaryDimension() != null ? b.getBoundaryDimension().name() : "DISTRICT",
                b.getBoundaryName(),
                b.getBoundaryId(),
                b.getPeriodType() != null ? b.getPeriodType().name() : "SEASONAL",
                b.getPeriodStart(),
                b.getPeriodEnd(),
                b.getOpeningBalanceM3(),
                b.getInflowReceivedM3(),
                b.getWaterTransferredInM3(),
                b.getWaterTransferredOutM3(),
                b.getWaterDeliveredM3(),
                b.getWaterConsumedM3(),
                b.getVerifiedLossesM3(),
                b.getRecordedReturnFlowM3(),
                b.getClosingBalanceM3(),
                b.getDiscrepancyM3(),
                b.getTolerancePercent(),
                b.isReconciled(),
                b.getEvidenceClassification() != null ? b.getEvidenceClassification().name() : "CALCULATED",
                b.getDataCompletenessPercent(),
                b.getNotes()
        );
    }

    public WaterLedgerEntryResponse toLedgerResponse(WaterLedgerEntry e) {
        if (e == null) return null;
        return new WaterLedgerEntryResponse(
                e.getId(),
                e.getEntryType().name(),
                e.getAccountingBoundary().name(),
                e.getVolumeM3(),
                e.getFlowRateM3S(),
                e.getSourceNodeOrCanal(),
                e.getDestinationNodeOrField(),
                e.getFieldId(),
                e.getWuaId(),
                e.getEdgeId(),
                e.getDispatchScheduleId(),
                e.getAllocationId(),
                e.getEventTimestamp(),
                e.getEvidenceClassification().name(),
                e.getDataSource(),
                e.getNotes(),
                e.getRecordedBy(),
                e.isCorrection(),
                e.getReplacesEntryId(),
                e.getCreatedAt()
        );
    }

    public ConveyanceLossResponse toLossResponse(ConveyanceLossAssessment l) {
        if (l == null) return null;
        return new ConveyanceLossResponse(
                l.getEdgeId(),
                l.getCanalName(),
                l.getEdgeType(),
                l.getStartNodeName(),
                l.getEndNodeName(),
                l.getPeriodStart(),
                l.getPeriodEnd(),
                l.getUpstreamVolumeM3(),
                l.getDownstreamVolumeM3(),
                l.getLossVolumeM3(),
                l.getLossPercentage(),
                l.getClassification().name(),
                l.getEvidenceClassification().name(),
                l.getConfidence(),
                l.getExplanation()
        );
    }

    public AllocationComplianceResponse toComplianceResponse(AllocationComplianceItem c) {
        if (c == null) return null;
        return new AllocationComplianceResponse(
                c.getTargetId(),
                c.getTargetName(),
                c.getTargetType(),
                c.getWuaName(),
                c.getCropType(),
                c.getAreaHectares(),
                c.getAllocatedVolumeM3(),
                c.getReservedVolumeM3(),
                c.getApprovedDispatchVolumeM3(),
                c.getActualDeliveredVolumeM3(),
                c.getActualConsumptionM3(),
                c.getRemainingAllocationM3(),
                c.getOverDeliveryM3(),
                c.getUnderDeliveryM3(),
                c.getDeliveryCompletionPercent(),
                c.getStatus().name(),
                c.getEvidenceClassification().name(),
                c.getNotes()
        );
    }

    public DistributionEquityResponse toEquityResponse(DistributionEquityAssessment e) {
        if (e == null) return null;
        return new DistributionEquityResponse(
                e.getClusterId(),
                e.getClusterName(),
                e.getClusterType(),
                e.getSampleCount(),
                e.getMeanDeliveryPerHectareM3(),
                e.getVarianceM3(),
                e.getCoefficientOfVariation(),
                e.getMeanAllocationFulfillmentPercent(),
                e.getAverageDeliveryDelayHours(),
                e.getDisparityLevel().name(),
                e.getFairnessPolicyApplied(),
                e.isConsistentWithPolicy(),
                e.getEvidenceClassification().name(),
                e.getSupportingEvidence(),
                e.getNotes()
        );
    }

    public ConservationOpportunityResponse toOpportunityResponse(ConservationOpportunity o) {
        if (o == null) return null;
        return new ConservationOpportunityResponse(
                o.getId(),
                o.getTitle(),
                o.getProblemDescription(),
                o.getCategory(),
                o.getAffectedComponentType(),
                o.getAffectedComponentId(),
                o.getAffectedComponentName(),
                o.getEstimatedPotentialSavingsM3(),
                o.getEstimationMethodology(),
                o.getConfidence(),
                o.getOperationalImpact(),
                o.getSuggestedAction(),
                o.getTargetWorkflowUri(),
                o.getEvidenceClassification().name(),
                o.getSupportingEvidence()
        );
    }

    public WaterReconciliationResponse toReconciliationResponse(WaterReconciliationItem r) {
        if (r == null) return null;
        return new WaterReconciliationResponse(
                r.getId(),
                r.getBoundaryType(),
                r.getBoundaryName(),
                r.getTargetId(),
                r.getPeriodStart(),
                r.getPeriodEnd(),
                r.getExpectedVolumeM3(),
                r.getRecordedVolumeM3(),
                r.getDiscrepancyVolumeM3(),
                r.getDiscrepancyPercent(),
                r.getTolerancePercent(),
                r.getStatus().name(),
                r.getEvidenceClassification().name(),
                r.getExplanation(),
                r.getReviewedBy(),
                r.getReviewedAt(),
                r.getReviewNotes(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
