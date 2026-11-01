package com.aquaai.dashboard.api;

import com.aquaai.dashboard.api.dto.DashboardOverviewResponse;
import com.aquaai.dashboard.application.usecase.GetDashboardOverviewUseCase;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) CropType cropType,
            @RequestParam(required = false) IrrigationPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        DashboardOverviewResponse response = getDashboardOverviewUseCase.execute(
                fieldId,
                cropType,
                priority,
                from,
                to
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
