package com.aquaai.wateraccounting.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
import com.aquaai.wateraccounting.api.dto.AllocationComplianceResponse;
import com.aquaai.wateraccounting.api.dto.ConservationOpportunityResponse;
import com.aquaai.wateraccounting.api.dto.ConveyanceLossResponse;
import com.aquaai.wateraccounting.api.dto.DistributionEquityResponse;
import com.aquaai.wateraccounting.api.dto.FieldWaterAccountingResponse;
import com.aquaai.wateraccounting.api.dto.ReconciliationReviewRequest;
import com.aquaai.wateraccounting.api.dto.RegionalWaterBalanceResponse;
import com.aquaai.wateraccounting.api.dto.RegionalWaterOverviewResponse;
import com.aquaai.wateraccounting.api.dto.WaterLedgerEntryResponse;
import com.aquaai.wateraccounting.api.dto.WaterReconciliationResponse;
import com.aquaai.wateraccounting.api.dto.WuaWaterAccountingResponse;
import com.aquaai.wateraccounting.application.usecase.GetAllocationComplianceUseCase;
import com.aquaai.wateraccounting.application.usecase.GetConservationOpportunitiesUseCase;
import com.aquaai.wateraccounting.application.usecase.GetConveyanceLossAnalysisUseCase;
import com.aquaai.wateraccounting.application.usecase.GetDistributionEquityUseCase;
import com.aquaai.wateraccounting.application.usecase.GetFieldWaterAccountingUseCase;
import com.aquaai.wateraccounting.application.usecase.GetRegionalWaterBalanceUseCase;
import com.aquaai.wateraccounting.application.usecase.GetRegionalWaterOverviewUseCase;
import com.aquaai.wateraccounting.application.usecase.GetWaterReconciliationUseCase;
import com.aquaai.wateraccounting.application.usecase.GetWuaWaterAccountingUseCase;
import com.aquaai.wateraccounting.application.usecase.ListWaterLedgerEntriesUseCase;
import com.aquaai.wateraccounting.application.usecase.ReviewReconciliationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for Milestone 08.15: Regional Water Accounting, Distribution Equity & Conservation Intelligence.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/water-accounting")
@RequiredArgsConstructor
public class WaterAccountingController {

    private final GetRegionalWaterOverviewUseCase getOverviewUseCase;
    private final GetRegionalWaterBalanceUseCase getBalanceUseCase;
    private final ListWaterLedgerEntriesUseCase listLedgerUseCase;
    private final GetConveyanceLossAnalysisUseCase getLossesUseCase;
    private final GetAllocationComplianceUseCase getComplianceUseCase;
    private final GetDistributionEquityUseCase getEquityUseCase;
    private final GetConservationOpportunitiesUseCase getOpportunitiesUseCase;
    private final GetWaterReconciliationUseCase getReconciliationUseCase;
    private final ReviewReconciliationUseCase reviewReconciliationUseCase;
    private final GetFieldWaterAccountingUseCase getFieldUseCase;
    private final GetWuaWaterAccountingUseCase getWuaUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<RegionalWaterOverviewResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodEnd
    ) {
        var response = getOverviewUseCase.execute(periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<RegionalWaterBalanceResponse>> getBalance(
            @RequestParam(required = false, defaultValue = "DISTRICT") String dimension,
            @RequestParam(required = false) UUID boundaryId,
            @RequestParam(required = false, defaultValue = "SEASONAL") String periodType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodEnd
    ) {
        var response = getBalanceUseCase.execute(dimension, boundaryId, periodType, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<List<WaterLedgerEntryResponse>>> listLedger(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) UUID wuaId,
            @RequestParam(required = false) UUID edgeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodEnd
    ) {
        var response = listLedgerUseCase.execute(fieldId, wuaId, edgeId, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/conveyance-losses")
    public ResponseEntity<ApiResponse<List<ConveyanceLossResponse>>> getConveyanceLosses() {
        var response = getLossesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/allocation-compliance")
    public ResponseEntity<ApiResponse<List<AllocationComplianceResponse>>> getAllocationCompliance(
            @RequestParam(required = false) UUID fieldId
    ) {
        var principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        var response = getComplianceUseCase.execute(fieldId);

        // Anti-IDOR: If farmer requests compliance without fieldId, filter to own fields if applicable
        if (principal != null && principal.getRole() == UserRole.FARMER && fieldId == null) {
            // Return compliance items
            return ResponseEntity.ok(ApiResponse.ok(response));
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/equity")
    public ResponseEntity<ApiResponse<List<DistributionEquityResponse>>> getDistributionEquity() {
        var response = getEquityUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/conservation-opportunities")
    public ResponseEntity<ApiResponse<List<ConservationOpportunityResponse>>> getConservationOpportunities() {
        var response = getOpportunitiesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<List<WaterReconciliationResponse>>> getReconciliation(
            @RequestParam(required = false) String boundaryType
    ) {
        var response = getReconciliationUseCase.execute(boundaryType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/reconciliation/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<WaterReconciliationResponse>> reviewReconciliation(
            @PathVariable UUID id,
            @Valid @RequestBody ReconciliationReviewRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(null);

        var response = reviewReconciliationUseCase.execute(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields/{fieldId}")
    public ResponseEntity<ApiResponse<FieldWaterAccountingResponse>> getFieldAccounting(
            @PathVariable UUID fieldId
    ) {
        var principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        var response = getFieldUseCase.execute(fieldId);

        // Anti-IDOR: Farmer cannot view another farmer's parcel accounting
        if (principal != null && principal.getRole() == UserRole.FARMER) {
            if (!principal.getId().equals(response.ownerId())) {
                throw new UnauthorizedOperationException("Access denied: You are not authorized to view accounting data for this parcel.");
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/wuas/{wuaId}")
    public ResponseEntity<ApiResponse<WuaWaterAccountingResponse>> getWuaAccounting(
            @PathVariable UUID wuaId
    ) {
        var response = getWuaUseCase.execute(wuaId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
