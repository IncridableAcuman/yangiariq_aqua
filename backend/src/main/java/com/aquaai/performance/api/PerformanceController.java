package com.aquaai.performance.api;

import com.aquaai.performance.api.dto.CreateBaselineRequest;
import com.aquaai.performance.api.dto.CreateImpactAssessmentRequest;
import com.aquaai.performance.api.dto.CreateTargetRequest;
import com.aquaai.performance.api.dto.ImpactAssessmentResponse;
import com.aquaai.performance.api.dto.PerformanceBaselineResponse;
import com.aquaai.performance.api.dto.PerformanceEvaluationResponse;
import com.aquaai.performance.api.dto.PerformanceIndicatorResponse;
import com.aquaai.performance.api.dto.PerformanceOverviewResponse;
import com.aquaai.performance.api.dto.PerformanceReportResponse;
import com.aquaai.performance.api.dto.PerformanceTargetResponse;
import com.aquaai.performance.api.dto.PerformanceTrendResponse;
import com.aquaai.performance.api.dto.ReviewImpactAssessmentRequest;
import com.aquaai.performance.api.dto.RunEvaluationRequest;
import com.aquaai.performance.application.dto.CreateBaselineCommand;
import com.aquaai.performance.application.dto.CreateImpactAssessmentCommand;
import com.aquaai.performance.application.dto.CreateTargetCommand;
import com.aquaai.performance.application.dto.PerformanceOverviewDto;
import com.aquaai.performance.application.dto.PerformanceReportDto;
import com.aquaai.performance.application.dto.ReviewImpactAssessmentCommand;
import com.aquaai.performance.application.dto.RunEvaluationCommand;
import com.aquaai.performance.application.usecase.CreateImpactAssessmentUseCase;
import com.aquaai.performance.application.usecase.CreatePerformanceBaselineUseCase;
import com.aquaai.performance.application.usecase.CreatePerformanceTargetUseCase;
import com.aquaai.performance.application.usecase.GeneratePerformanceReportUseCase;
import com.aquaai.performance.application.usecase.GetPerformanceEvaluationDetailUseCase;
import com.aquaai.performance.application.usecase.GetPerformanceIndicatorDetailUseCase;
import com.aquaai.performance.application.usecase.GetPerformanceOverviewUseCase;
import com.aquaai.performance.application.usecase.GetPerformanceTrendUseCase;
import com.aquaai.performance.application.usecase.ListImpactAssessmentsUseCase;
import com.aquaai.performance.application.usecase.ListPerformanceBaselinesUseCase;
import com.aquaai.performance.application.usecase.ListPerformanceIndicatorsUseCase;
import com.aquaai.performance.application.usecase.ListPerformanceTargetsUseCase;
import com.aquaai.performance.application.usecase.ReviewImpactAssessmentUseCase;
import com.aquaai.performance.application.usecase.RunPerformanceEvaluationUseCase;
import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.model.IndicatorCategory;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.port.PerformanceFieldPort;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserRole;
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
 * REST controller for Milestone 08.17: Regional Water Operations Performance, Outcome Measurement & Impact Assessment.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final GetPerformanceOverviewUseCase overviewUseCase;
    private final ListPerformanceIndicatorsUseCase listIndicatorsUseCase;
    private final GetPerformanceIndicatorDetailUseCase indicatorDetailUseCase;
    private final ListPerformanceBaselinesUseCase listBaselinesUseCase;
    private final CreatePerformanceBaselineUseCase createBaselineUseCase;
    private final ListPerformanceTargetsUseCase listTargetsUseCase;
    private final CreatePerformanceTargetUseCase createTargetUseCase;
    private final RunPerformanceEvaluationUseCase runEvaluationUseCase;
    private final GetPerformanceEvaluationDetailUseCase evaluationDetailUseCase;
    private final ListImpactAssessmentsUseCase listImpactAssessmentsUseCase;
    private final CreateImpactAssessmentUseCase createImpactAssessmentUseCase;
    private final ReviewImpactAssessmentUseCase reviewImpactAssessmentUseCase;
    private final GetPerformanceTrendUseCase trendUseCase;
    private final GeneratePerformanceReportUseCase reportUseCase;
    private final PerformanceFieldPort fieldPort;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<PerformanceOverviewResponse>> getOverview() {
        PerformanceOverviewDto dto = overviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(toOverviewResponse(dto)));
    }

    @GetMapping("/indicators")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<List<PerformanceIndicatorResponse>>> listIndicators(
            @RequestParam(required = false) IndicatorCategory category
    ) {
        List<PerformanceIndicator> indicators = listIndicatorsUseCase.execute(category);
        return ResponseEntity.ok(ApiResponse.ok(
                indicators.stream().map(this::toIndicatorResponse).collect(Collectors.toList())
        ));
    }

    @GetMapping("/indicators/{code}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<PerformanceIndicatorResponse>> getIndicator(
            @PathVariable String code
    ) {
        PerformanceIndicator indicator = indicatorDetailUseCase.execute(code);
        return ResponseEntity.ok(ApiResponse.ok(toIndicatorResponse(indicator)));
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<PerformanceTrendResponse>> getTrends(
            @RequestParam(defaultValue = "WDP_DELIVERY_FULFILLMENT") String indicatorCode,
            @RequestParam(defaultValue = "MONTHLY") String interval
    ) {
        var trendDto = trendUseCase.execute(indicatorCode, interval);
        return ResponseEntity.ok(ApiResponse.ok(
                PerformanceTrendResponse.builder()
                        .indicatorCode(trendDto.getIndicatorCode())
                        .indicatorName(trendDto.getIndicatorName())
                        .measurementUnit(trendDto.getMeasurementUnit())
                        .dataPoints(trendDto.getDataPoints())
                        .build()
        ));
    }

    @GetMapping("/baselines")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<List<PerformanceBaselineResponse>>> listBaselines(
            @RequestParam(required = false) String indicatorCode,
            @RequestParam(required = false) GeographicScopeType scopeType
    ) {
        List<PerformanceBaseline> baselines = listBaselinesUseCase.execute(indicatorCode, scopeType);
        return ResponseEntity.ok(ApiResponse.ok(
                baselines.stream().map(this::toBaselineResponse).collect(Collectors.toList())
        ));
    }

    @PostMapping("/baselines")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PerformanceBaselineResponse>> createBaseline(
            @Valid @RequestBody CreateBaselineRequest request
    ) {
        var principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        var command = CreateBaselineCommand.builder()
                .indicatorCode(request.getIndicatorCode())
                .name(request.getName())
                .description(request.getDescription())
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .scopeName(request.getScopeName())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .baselineValue(request.getBaselineValue())
                .measurementUnit(request.getMeasurementUnit())
                .dataCompletenessPct(request.getDataCompletenessPct())
                .evidenceClassification(request.getEvidenceClassification())
                .baselineQuality(request.getBaselineQuality())
                .calculationMethod(request.getCalculationMethod())
                .seasonContext(request.getSeasonContext())
                .approvedBy(principal != null ? principal.getId() : null)
                .build();

        PerformanceBaseline created = createBaselineUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(toBaselineResponse(created)));
    }

    @GetMapping("/targets")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<List<PerformanceTargetResponse>>> listTargets(
            @RequestParam(required = false) String indicatorCode,
            @RequestParam(required = false) GeographicScopeType scopeType
    ) {
        List<PerformanceTarget> targets = listTargetsUseCase.execute(indicatorCode, scopeType);
        return ResponseEntity.ok(ApiResponse.ok(
                targets.stream().map(this::toTargetResponse).collect(Collectors.toList())
        ));
    }

    @PostMapping("/targets")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PerformanceTargetResponse>> createTarget(
            @Valid @RequestBody CreateTargetRequest request
    ) {
        var command = CreateTargetCommand.builder()
                .indicatorCode(request.getIndicatorCode())
                .title(request.getTitle())
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .scopeName(request.getScopeName())
                .targetType(request.getTargetType())
                .targetCategory(request.getTargetCategory())
                .targetValue(request.getTargetValue())
                .rangeMin(request.getRangeMin())
                .rangeMax(request.getRangeMax())
                .measurementUnit(request.getMeasurementUnit())
                .effectiveStart(request.getEffectiveStart())
                .effectiveEnd(request.getEffectiveEnd())
                .targetOwner(request.getTargetOwner())
                .rationale(request.getRationale())
                .build();

        PerformanceTarget created = createTargetUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(toTargetResponse(created)));
    }

    @PostMapping("/evaluations")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PerformanceEvaluationResponse>> runEvaluation(
            @Valid @RequestBody RunEvaluationRequest request
    ) {
        var command = RunEvaluationCommand.builder()
                .indicatorCode(request.getIndicatorCode())
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .customValue(request.getCustomValue())
                .calculationVersion(request.getCalculationVersion())
                .build();

        PerformanceEvaluation evaluation = runEvaluationUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(toEvaluationResponse(evaluation)));
    }

    @GetMapping("/evaluations/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<PerformanceEvaluationResponse>> getEvaluation(
            @PathVariable UUID id
    ) {
        PerformanceEvaluation evaluation = evaluationDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(toEvaluationResponse(evaluation)));
    }

    @GetMapping("/impact-assessments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    public ResponseEntity<ApiResponse<List<ImpactAssessmentResponse>>> listImpactAssessments(
            @RequestParam(required = false) String interventionType,
            @RequestParam(required = false) AttributionLevel attributionLevel,
            @RequestParam(required = false) AssessmentReviewStatus reviewStatus,
            @RequestParam(required = false) UUID scopeId
    ) {
        // Enforce anti-IDOR for FARMER role if scopeId is a field
        checkFarmerAccess(scopeId);

        List<ImpactAssessment> assessments = listImpactAssessmentsUseCase.execute(
                interventionType, attributionLevel, reviewStatus, scopeId
        );
        return ResponseEntity.ok(ApiResponse.ok(
                assessments.stream().map(this::toImpactResponse).collect(Collectors.toList())
        ));
    }

    @PostMapping("/impact-assessments")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ImpactAssessmentResponse>> createImpactAssessment(
            @Valid @RequestBody CreateImpactAssessmentRequest request
    ) {
        var command = CreateImpactAssessmentCommand.builder()
                .title(request.getTitle())
                .interventionType(request.getInterventionType())
                .interventionId(request.getInterventionId())
                .interventionReference(request.getInterventionReference())
                .interventionDate(request.getInterventionDate())
                .indicatorCode(request.getIndicatorCode())
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .scopeName(request.getScopeName())
                .baselineId(request.getBaselineId())
                .baselinePeriodStart(request.getBaselinePeriodStart())
                .baselinePeriodEnd(request.getBaselinePeriodEnd())
                .baselineValue(request.getBaselineValue())
                .evaluationPeriodStart(request.getEvaluationPeriodStart())
                .evaluationPeriodEnd(request.getEvaluationPeriodEnd())
                .postInterventionValue(request.getPostInterventionValue())
                .measurementUnit(request.getMeasurementUnit())
                .evidenceClassification(request.getEvidenceClassification())
                .baselineQuality(request.getBaselineQuality())
                .attributionLevel(request.getAttributionLevel())
                .confoundingFactors(request.getConfoundingFactors())
                .dataLimitations(request.getDataLimitations())
                .methodologyNotes(request.getMethodologyNotes())
                .build();

        ImpactAssessment assessment = createImpactAssessmentUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(toImpactResponse(assessment)));
    }

    @PostMapping("/impact-assessments/{id}/review")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ImpactAssessmentResponse>> reviewImpactAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewImpactAssessmentRequest request
    ) {
        var principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        var command = ReviewImpactAssessmentCommand.builder()
                .assessmentId(id)
                .reviewStatus(request.getReviewStatus())
                .reviewerId(principal != null ? principal.getId() : null)
                .reviewerName(principal != null ? principal.getUsername() : "Operational Reviewer")
                .reviewNotes(request.getReviewNotes())
                .build();

        ImpactAssessment updated = reviewImpactAssessmentUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(toImpactResponse(updated)));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PerformanceReportResponse>> generateReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String scope
    ) {
        PerformanceReportDto reportDto = reportUseCase.execute(startDate, endDate, scope);
        return ResponseEntity.ok(ApiResponse.ok(
                PerformanceReportResponse.builder()
                        .reportId(reportDto.getReportId())
                        .reportTitle(reportDto.getReportTitle())
                        .generatedAt(reportDto.getGeneratedAt())
                        .generatedBy(reportDto.getGeneratedBy())
                        .reportingPeriodStart(reportDto.getReportingPeriodStart())
                        .reportingPeriodEnd(reportDto.getReportingPeriodEnd())
                        .geographicScope(reportDto.getGeographicScope())
                        .legalDisclaimer(reportDto.getLegalDisclaimer())
                        .overview(reportDto.getOverview())
                        .indicators(reportDto.getIndicators())
                        .evaluations(reportDto.getEvaluations())
                        .impactAssessments(reportDto.getImpactAssessments())
                        .build()
        ));
    }

    private void checkFarmerAccess(UUID scopeId) {
        var principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        if (principal != null && principal.getRole() == UserRole.FARMER && scopeId != null) {
            boolean owned = fieldPort.isFieldOwnedBy(scopeId, principal.getId());
            if (!owned) {
                log.warn("IDOR attempt detected: Farmer {} attempted unauthorized access to scope ID {}", principal.getId(), scopeId);
                throw new UnauthorizedOperationException("Access denied: You do not own the specified agricultural parcel.");
            }
        }
    }

    private PerformanceOverviewResponse toOverviewResponse(PerformanceOverviewDto dto) {
        return PerformanceOverviewResponse.builder()
                .totalWaterDeliveredM3(dto.getTotalWaterDeliveredM3())
                .totalWaterDeliveredEvidence(dto.getTotalWaterDeliveredEvidence())
                .deliveryFulfillmentPct(dto.getDeliveryFulfillmentPct())
                .deliveryFulfillmentEvidence(dto.getDeliveryFulfillmentEvidence())
                .averageConveyanceLossPct(dto.getAverageConveyanceLossPct())
                .totalLossVolumeM3(dto.getTotalLossVolumeM3())
                .conveyanceLossEvidence(dto.getConveyanceLossEvidence())
                .quotaCompliancePct(dto.getQuotaCompliancePct())
                .quotaComplianceEvidence(dto.getQuotaComplianceEvidence())
                .distributionEquityCv(dto.getDistributionEquityCv())
                .distributionEquityLevel(dto.getDistributionEquityLevel())
                .distributionEquityEvidence(dto.getDistributionEquityEvidence())
                .totalIncidents(dto.getTotalIncidents())
                .overdueIncidents(dto.getOverdueIncidents())
                .verifiedResolvedIncidents(dto.getVerifiedResolvedIncidents())
                .verifiedResolutionRatePct(dto.getVerifiedResolutionRatePct())
                .incidentRecurrenceRatePct(dto.getIncidentRecurrenceRatePct())
                .accountabilityEvidence(dto.getAccountabilityEvidence())
                .sensorObservedCoveragePct(dto.getSensorObservedCoveragePct())
                .activeSensorsCount(dto.getActiveSensorsCount())
                .dataQualityEvidence(dto.getDataQualityEvidence())
                .verifiedConservationSavingsM3(dto.getVerifiedConservationSavingsM3())
                .conservationEvidence(dto.getConservationEvidence())
                .build();
    }

    private PerformanceIndicatorResponse toIndicatorResponse(PerformanceIndicator i) {
        return PerformanceIndicatorResponse.builder()
                .id(i.getId())
                .code(i.getCode())
                .name(i.getName())
                .description(i.getDescription())
                .category(i.getCategory())
                .measurementUnit(i.getMeasurementUnit())
                .calculationMethod(i.getCalculationMethod())
                .aggregationMethod(i.getAggregationMethod())
                .supportedGeographicScope(i.getSupportedGeographicScope())
                .evidenceRequirement(i.getEvidenceRequirement())
                .minimumDataQualityPct(i.getMinimumDataQualityPct())
                .applicableTimePeriod(i.getApplicableTimePeriod())
                .sourceModule(i.getSourceModule())
                .calculationVersion(i.getCalculationVersion())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private PerformanceBaselineResponse toBaselineResponse(PerformanceBaseline b) {
        return PerformanceBaselineResponse.builder()
                .id(b.getId())
                .indicatorCode(b.getIndicatorCode())
                .name(b.getName())
                .description(b.getDescription())
                .scopeType(b.getScopeType())
                .scopeId(b.getScopeId())
                .scopeName(b.getScopeName())
                .periodStart(b.getPeriodStart())
                .periodEnd(b.getPeriodEnd())
                .baselineValue(b.getBaselineValue())
                .measurementUnit(b.getMeasurementUnit())
                .dataCompletenessPct(b.getDataCompletenessPct())
                .evidenceClassification(b.getEvidenceClassification())
                .baselineQuality(b.getBaselineQuality())
                .calculationMethod(b.getCalculationMethod())
                .seasonContext(b.getSeasonContext())
                .approvalStatus(b.getApprovalStatus())
                .approvedBy(b.getApprovedBy())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private PerformanceTargetResponse toTargetResponse(PerformanceTarget t) {
        return PerformanceTargetResponse.builder()
                .id(t.getId())
                .indicatorCode(t.getIndicatorCode())
                .title(t.getTitle())
                .scopeType(t.getScopeType())
                .scopeId(t.getScopeId())
                .scopeName(t.getScopeName())
                .targetType(t.getTargetType())
                .targetCategory(t.getTargetCategory())
                .targetValue(t.getTargetValue())
                .rangeMin(t.getRangeMin())
                .rangeMax(t.getRangeMax())
                .measurementUnit(t.getMeasurementUnit())
                .effectiveStart(t.getEffectiveStart())
                .effectiveEnd(t.getEffectiveEnd())
                .targetOwner(t.getTargetOwner())
                .status(t.getStatus())
                .rationale(t.getRationale())
                .targetVersion(t.getTargetVersion())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private PerformanceEvaluationResponse toEvaluationResponse(PerformanceEvaluation e) {
        return PerformanceEvaluationResponse.builder()
                .id(e.getId())
                .evaluationNumber(e.getEvaluationNumber())
                .indicatorCode(e.getIndicatorCode())
                .scopeType(e.getScopeType())
                .scopeId(e.getScopeId())
                .scopeName(e.getScopeName())
                .periodStart(e.getPeriodStart())
                .periodEnd(e.getPeriodEnd())
                .calculatedValue(e.getCalculatedValue())
                .measurementUnit(e.getMeasurementUnit())
                .baselineId(e.getBaselineId())
                .baselineValue(e.getBaselineValue())
                .targetId(e.getTargetId())
                .targetValue(e.getTargetValue())
                .targetCompliance(e.getTargetCompliance())
                .varianceFromBaselinePct(e.getVarianceFromBaselinePct())
                .varianceFromTargetPct(e.getVarianceFromTargetPct())
                .trend(e.getTrend())
                .evidenceClassification(e.getEvidenceClassification())
                .observedSampleRatio(e.getObservedSampleRatio())
                .missingDataIntervals(e.getMissingDataIntervals())
                .comparisonValid(e.isComparisonValid())
                .validityNotes(e.getValidityNotes())
                .explanatorySummary(e.getExplanatorySummary())
                .calculationVersion(e.getCalculationVersion())
                .evaluatedAt(e.getEvaluatedAt())
                .build();
    }

    private ImpactAssessmentResponse toImpactResponse(ImpactAssessment a) {
        return ImpactAssessmentResponse.builder()
                .id(a.getId())
                .assessmentNumber(a.getAssessmentNumber())
                .title(a.getTitle())
                .interventionType(a.getInterventionType())
                .interventionId(a.getInterventionId())
                .interventionReference(a.getInterventionReference())
                .interventionDate(a.getInterventionDate())
                .indicatorCode(a.getIndicatorCode())
                .scopeType(a.getScopeType())
                .scopeId(a.getScopeId())
                .scopeName(a.getScopeName())
                .baselineId(a.getBaselineId())
                .baselinePeriodStart(a.getBaselinePeriodStart())
                .baselinePeriodEnd(a.getBaselinePeriodEnd())
                .baselineValue(a.getBaselineValue())
                .evaluationPeriodStart(a.getEvaluationPeriodStart())
                .evaluationPeriodEnd(a.getEvaluationPeriodEnd())
                .postInterventionValue(a.getPostInterventionValue())
                .absoluteChange(a.getAbsoluteChange())
                .percentageChange(a.getPercentageChange())
                .changeDirection(a.getChangeDirection())
                .measurementUnit(a.getMeasurementUnit())
                .evidenceClassification(a.getEvidenceClassification())
                .baselineQuality(a.getBaselineQuality())
                .attributionLevel(a.getAttributionLevel())
                .confoundingFactors(a.getConfoundingFactors())
                .dataLimitations(a.getDataLimitations())
                .comparisonValid(a.isComparisonValid())
                .methodologyNotes(a.getMethodologyNotes())
                .reviewStatus(a.getReviewStatus())
                .reviewerId(a.getReviewerId())
                .reviewerName(a.getReviewerName())
                .reviewNotes(a.getReviewNotes())
                .reviewedAt(a.getReviewedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
