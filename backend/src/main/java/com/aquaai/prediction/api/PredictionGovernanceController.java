package com.aquaai.prediction.api;

import com.aquaai.prediction.application.dto.ActivateVersionRequest;
import com.aquaai.prediction.application.dto.ApproveProposalRequest;
import com.aquaai.prediction.application.dto.BacktestRunDto;
import com.aquaai.prediction.application.dto.CalibrationProposalDetailResponse;
import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.application.dto.GovernanceAuditLogDto;
import com.aquaai.prediction.application.dto.GovernanceOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionMethodVersionDto;
import com.aquaai.prediction.application.dto.RejectProposalRequest;
import com.aquaai.prediction.application.dto.RollbackVersionRequest;
import com.aquaai.prediction.application.usecase.ActivateVersionUseCase;
import com.aquaai.prediction.application.usecase.ApproveCalibrationProposalUseCase;
import com.aquaai.prediction.application.usecase.GenerateCalibrationProposalUseCase;
import com.aquaai.prediction.application.usecase.GetCalibrationProposalDetailUseCase;
import com.aquaai.prediction.application.usecase.GetCalibrationProposalsUseCase;
import com.aquaai.prediction.application.usecase.GetGovernanceAuditTrailUseCase;
import com.aquaai.prediction.application.usecase.GetGovernanceOverviewUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionMethodVersionsUseCase;
import com.aquaai.prediction.application.usecase.RejectCalibrationProposalUseCase;
import com.aquaai.prediction.application.usecase.RollbackVersionUseCase;
import com.aquaai.prediction.application.usecase.RunBacktestUseCase;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserPrincipal;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for Milestone 08.9: Adaptive Intelligence, Model Governance & Controlled Calibration.
 */
@RestController
@RequestMapping("/api/v1/predictions/governance")
@RequiredArgsConstructor
public class PredictionGovernanceController {

    private final GetGovernanceOverviewUseCase getGovernanceOverviewUseCase;
    private final GetPredictionMethodVersionsUseCase getPredictionMethodVersionsUseCase;
    private final GetCalibrationProposalsUseCase getCalibrationProposalsUseCase;
    private final GetCalibrationProposalDetailUseCase getCalibrationProposalDetailUseCase;
    private final RunBacktestUseCase runBacktestUseCase;
    private final ApproveCalibrationProposalUseCase approveCalibrationProposalUseCase;
    private final RejectCalibrationProposalUseCase rejectCalibrationProposalUseCase;
    private final ActivateVersionUseCase activateVersionUseCase;
    private final RollbackVersionUseCase rollbackVersionUseCase;
    private final GetGovernanceAuditTrailUseCase getGovernanceAuditTrailUseCase;
    private final GenerateCalibrationProposalUseCase generateCalibrationProposalUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<GovernanceOverviewResponse>> getOverview() {
        GovernanceOverviewResponse response = getGovernanceOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/versions")
    public ResponseEntity<ApiResponse<List<PredictionMethodVersionDto>>> getVersions(
            @RequestParam(required = false) String type
    ) {
        PredictionType pType = parsePredictionType(type);
        List<PredictionMethodVersionDto> response = getPredictionMethodVersionsUseCase.execute(pType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/proposals")
    public ResponseEntity<ApiResponse<List<CalibrationProposalDto>>> getProposals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "false") boolean openOnly
    ) {
        PredictionType pType = parsePredictionType(type);
        List<CalibrationProposalDto> response = getCalibrationProposalsUseCase.execute(pType, openOnly);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<CalibrationProposalDetailResponse>> getProposalDetail(
            @PathVariable UUID proposalId
    ) {
        CalibrationProposalDetailResponse response = getCalibrationProposalDetailUseCase.execute(proposalId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/proposals/{proposalId}/backtest")
    public ResponseEntity<ApiResponse<BacktestRunDto>> runBacktest(
            @PathVariable UUID proposalId
    ) {
        UserPrincipal principal = SecurityContextHelper.getCurrentPrincipal().orElse(null);
        String actorId = principal != null ? principal.getUsername() : "SYSTEM";
        String actorRole = principal != null ? principal.getRole().name() : "ADMIN";

        BacktestRunDto response = runBacktestUseCase.execute(proposalId, actorId, actorRole);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/proposals/{proposalId}/approve")
    public ResponseEntity<ApiResponse<CalibrationProposalDto>> approveProposal(
            @PathVariable UUID proposalId,
            @RequestBody(required = false) ApproveProposalRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        String notes = request != null ? request.notes() : null;

        CalibrationProposalDto response = approveCalibrationProposalUseCase.execute(
                proposalId, principal.getUsername(), principal.getRole().name(), notes);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/proposals/{proposalId}/reject")
    public ResponseEntity<ApiResponse<CalibrationProposalDto>> rejectProposal(
            @PathVariable UUID proposalId,
            @RequestBody(required = false) RejectProposalRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        String reason = request != null ? request.reason() : "Rejected by administrator.";

        CalibrationProposalDto response = rejectCalibrationProposalUseCase.execute(
                proposalId, principal.getUsername(), principal.getRole().name(), reason);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/versions/{versionId}/activate")
    public ResponseEntity<ApiResponse<PredictionMethodVersionDto>> activateVersion(
            @PathVariable UUID versionId,
            @RequestBody(required = false) ActivateVersionRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        String mode = request != null ? request.rolloutMode() : "ACTIVE";
        String reason = request != null ? request.reason() : "Activated in production.";

        PredictionMethodVersionDto response = activateVersionUseCase.execute(
                versionId, mode, principal.getUsername(), principal.getRole().name(), reason);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/versions/rollback")
    public ResponseEntity<ApiResponse<PredictionMethodVersionDto>> rollbackVersion(
            @RequestParam String type,
            @RequestBody RollbackVersionRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        PredictionType pType = parsePredictionType(type);
        if (pType == null) {
            throw new IllegalArgumentException("Valid prediction type is required for rollback.");
        }

        PredictionMethodVersionDto response = rollbackVersionUseCase.execute(
                pType, request.priorVersionTag(), principal.getUsername(), principal.getRole().name(), request.reason());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/audit-trail")
    public ResponseEntity<ApiResponse<List<GovernanceAuditLogDto>>> getAuditTrail(
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        List<GovernanceAuditLogDto> response = getGovernanceAuditTrailUseCase.execute(limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/proposals/generate")
    public ResponseEntity<ApiResponse<CalibrationProposalDto>> generateProposal(
            @RequestParam String type
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        PredictionType pType = parsePredictionType(type);
        if (pType == null) {
            throw new IllegalArgumentException("Valid prediction type is required.");
        }

        Optional<CalibrationProposalDto> responseOpt = generateCalibrationProposalUseCase.execute(
                pType, principal.getUsername(), principal.getRole().name());

        return responseOpt
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private void enforceAdminRole() {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only system administrators can approve, activate, or rollback model versions.");
        }
    }

    private PredictionType parsePredictionType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return PredictionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
