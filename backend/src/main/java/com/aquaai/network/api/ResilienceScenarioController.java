package com.aquaai.network.api;

import com.aquaai.network.api.dto.ResilienceScenarioRequest;
import com.aquaai.network.api.dto.ResilienceScenarioResponse;
import com.aquaai.network.application.usecase.GetResilienceScenarioDetailUseCase;
import com.aquaai.network.application.usecase.ListResilienceScenariosUseCase;
import com.aquaai.network.application.usecase.SimulateResilienceScenarioUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Milestone 08.14: Network Resilience Scenarios.
 * Computes isolated disruption impact simulations (canal outages, pump failures, drought deficits).
 * STRICT POLICY: Simulation only. Never alters live network topology or active schedules.
 */
@RestController
@RequestMapping("/api/v1/water-network/scenarios")
@RequiredArgsConstructor
public class ResilienceScenarioController {

    private final SimulateResilienceScenarioUseCase simulateResilienceScenarioUseCase;
    private final ListResilienceScenariosUseCase listResilienceScenariosUseCase;
    private final GetResilienceScenarioDetailUseCase getResilienceScenarioDetailUseCase;

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AGRONOMIST')")
    public ResponseEntity<ApiResponse<ResilienceScenarioResponse>> simulateScenario(
            @Valid @RequestBody ResilienceScenarioRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        ResilienceScenarioResponse response = simulateResilienceScenarioUseCase.execute(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResilienceScenarioResponse>>> listScenarios() {
        List<ResilienceScenarioResponse> response = listResilienceScenariosUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResilienceScenarioResponse>> getScenarioDetail(
            @PathVariable UUID id
    ) {
        ResilienceScenarioResponse response = getResilienceScenarioDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
