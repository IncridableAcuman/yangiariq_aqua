package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.PilotReadinessResponse;
import com.aquaai.pilot.application.usecase.PilotReadinessUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pilot-readiness")
@Tag(name = "Pilot Readiness Assessment", description = "Endpoints for assessing pre-flight operational readiness across Infrastructure, Data, Operations, and Governance")
@SecurityRequirement(name = "bearerAuth")
public class PilotReadinessController {

    private final PilotReadinessUseCase readinessUseCase;

    public PilotReadinessController(PilotReadinessUseCase readinessUseCase) {
        this.readinessUseCase = readinessUseCase;
    }

    @GetMapping("/{pilotId}")
    @Operation(summary = "Get latest readiness assessment", description = "Retrieves the most recent pre-flight readiness assessment for a pilot")
    public ResponseEntity<ApiResponse<PilotReadinessResponse>> getLatestReadiness(@PathVariable UUID pilotId) {
        PilotReadinessResponse response = readinessUseCase.getLatestReadiness(pilotId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{pilotId}/assess")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST')")
    @Operation(summary = "Trigger readiness evaluation", description = "Executes multi-dimensional readiness checks and persists an assessment record")
    public ResponseEntity<ApiResponse<PilotReadinessResponse>> assessReadiness(@PathVariable UUID pilotId) {
        UUID assessorId = SecurityContextHelper.getCurrentUserId();
        PilotReadinessResponse response = readinessUseCase.assessReadiness(pilotId, assessorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
