package com.aquaai.prediction.api;

import com.aquaai.prediction.application.dto.EvaluateBatchResponse;
import com.aquaai.prediction.application.dto.EvaluationHistoryResponse;
import com.aquaai.prediction.application.dto.FieldEvaluationResponse;
import com.aquaai.prediction.application.dto.FieldPredictionsResponse;
import com.aquaai.prediction.application.dto.ForecastHistoryResponse;
import com.aquaai.prediction.application.dto.IrrigationNeedRankingResponse;
import com.aquaai.prediction.application.dto.PredictionEvaluationDetailDto;
import com.aquaai.prediction.application.dto.PredictionEvaluationOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionItemDto;
import com.aquaai.prediction.application.dto.PredictionOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionTypePerformanceDto;
import com.aquaai.prediction.application.dto.WaterDemandForecastResponse;
import com.aquaai.prediction.application.usecase.EvaluatePendingPredictionsUseCase;
import com.aquaai.prediction.application.usecase.GetEmergingRisksUseCase;
import com.aquaai.prediction.application.usecase.GetFarmPredictionOverviewUseCase;
import com.aquaai.prediction.application.usecase.GetFieldPredictionsUseCase;
import com.aquaai.prediction.application.usecase.GetForecastHistoryUseCase;
import com.aquaai.prediction.application.usecase.GetIrrigationNeedRankingUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionDetailEvaluationUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionEvaluationByFieldUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionEvaluationByTypeUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionEvaluationHistoryUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionEvaluationOverviewUseCase;
import com.aquaai.prediction.application.usecase.GetWaterDemandForecastUseCase;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Predictive Farm Intelligence, Forecasting & Model Evaluation (Milestones 08.7 & 08.8).
 */
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final GetFarmPredictionOverviewUseCase getFarmPredictionOverviewUseCase;
    private final GetFieldPredictionsUseCase getFieldPredictionsUseCase;
    private final GetWaterDemandForecastUseCase getWaterDemandForecastUseCase;
    private final GetIrrigationNeedRankingUseCase getIrrigationNeedRankingUseCase;
    private final GetEmergingRisksUseCase getEmergingRisksUseCase;
    private final GetForecastHistoryUseCase getForecastHistoryUseCase;

    // --- Milestone 08.8 Continuous Intelligence & Evaluation Use Cases ---
    private final GetPredictionEvaluationOverviewUseCase getPredictionEvaluationOverviewUseCase;
    private final GetPredictionEvaluationByTypeUseCase getPredictionEvaluationByTypeUseCase;
    private final GetPredictionEvaluationByFieldUseCase getPredictionEvaluationByFieldUseCase;
    private final GetPredictionEvaluationHistoryUseCase getPredictionEvaluationHistoryUseCase;
    private final GetPredictionDetailEvaluationUseCase getPredictionDetailEvaluationUseCase;
    private final EvaluatePendingPredictionsUseCase evaluatePendingPredictionsUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<PredictionOverviewResponse>> getOverview(
            @RequestParam(required = false, defaultValue = "NEXT_48_HOURS") String horizon
    ) {
        PredictionOverviewResponse response = getFarmPredictionOverviewUseCase.execute(horizon);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields/{fieldId}")
    public ResponseEntity<ApiResponse<FieldPredictionsResponse>> getFieldPredictions(
            @PathVariable UUID fieldId,
            @RequestParam(required = false, defaultValue = "NEXT_48_HOURS") String horizon
    ) {
        FieldPredictionsResponse response = getFieldPredictionsUseCase.execute(fieldId, horizon);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/water-demand")
    public ResponseEntity<ApiResponse<WaterDemandForecastResponse>> getWaterDemandForecast(
            @RequestParam(required = false, defaultValue = "NEXT_48_HOURS") String horizon
    ) {
        WaterDemandForecastResponse response = getWaterDemandForecastUseCase.execute(horizon);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/irrigation-need")
    public ResponseEntity<ApiResponse<IrrigationNeedRankingResponse>> getIrrigationNeedRanking(
            @RequestParam(required = false, defaultValue = "NEXT_48_HOURS") String horizon
    ) {
        IrrigationNeedRankingResponse response = getIrrigationNeedRankingUseCase.execute(horizon);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/risks")
    public ResponseEntity<ApiResponse<List<PredictionItemDto>>> getEmergingRisks(
            @RequestParam(required = false, defaultValue = "NEXT_48_HOURS") String horizon
    ) {
        List<PredictionItemDto> response = getEmergingRisksUseCase.execute(horizon);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<ForecastHistoryResponse>> getForecastHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        ForecastHistoryResponse response = getForecastHistoryUseCase.execute(from, to, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // =========================================================================
    // Milestone 08.8: Predictive Model Evaluation & Continuous Intelligence API
    // =========================================================================

    @GetMapping("/evaluation/overview")
    public ResponseEntity<ApiResponse<PredictionEvaluationOverviewResponse>> getEvaluationOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        PredictionEvaluationOverviewResponse response = getPredictionEvaluationOverviewUseCase.execute(from, to);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/evaluation/by-type")
    public ResponseEntity<ApiResponse<List<PredictionTypePerformanceDto>>> getEvaluationByType(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        PredictionType pType = null;
        if (type != null && !type.isBlank()) {
            try {
                pType = PredictionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        List<PredictionTypePerformanceDto> response = getPredictionEvaluationByTypeUseCase.execute(pType, from, to);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/evaluation/by-field")
    public ResponseEntity<ApiResponse<FieldEvaluationResponse>> getEvaluationByField(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        FieldEvaluationResponse response = getPredictionEvaluationByFieldUseCase.execute(from, to, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/evaluation/history")
    public ResponseEntity<ApiResponse<EvaluationHistoryResponse>> getEvaluationHistory(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        EvaluationHistoryResponse response = getPredictionEvaluationHistoryUseCase.execute(
                fieldId, type, status, from, to, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{predictionId}/evaluation")
    public ResponseEntity<ApiResponse<PredictionEvaluationDetailDto>> getPredictionDetailEvaluation(
            @PathVariable UUID predictionId
    ) {
        PredictionEvaluationDetailDto response = getPredictionDetailEvaluationUseCase.execute(predictionId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/evaluation/evaluate")
    public ResponseEntity<ApiResponse<EvaluateBatchResponse>> triggerEvaluation() {
        EvaluateBatchResponse response = evaluatePendingPredictionsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
