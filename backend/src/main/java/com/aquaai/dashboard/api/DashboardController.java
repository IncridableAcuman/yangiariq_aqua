package com.aquaai.dashboard.api;

import com.aquaai.dashboard.api.dto.DistrictDashboardSummary;
import com.aquaai.dashboard.application.DashboardService;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Read-oriented district summary and operational analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get high-level operational overview for Yangiariq district")
    public ResponseEntity<ApiResponse<DistrictDashboardSummary>> getSummary() {
        DistrictDashboardSummary summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
