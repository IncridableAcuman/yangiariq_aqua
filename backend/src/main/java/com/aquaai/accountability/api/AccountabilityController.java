package com.aquaai.accountability.api;

import com.aquaai.accountability.api.dto.AccountabilityDashboardResponse;
import com.aquaai.accountability.api.dto.ApproveActionRequest;
import com.aquaai.accountability.api.dto.AssignInvestigatorRequest;
import com.aquaai.accountability.api.dto.AttachEvidenceRequest;
import com.aquaai.accountability.api.dto.CompleteActionRequest;
import com.aquaai.accountability.api.dto.CreateIncidentRequest;
import com.aquaai.accountability.api.dto.CorrectiveActionResponse;
import com.aquaai.accountability.api.dto.IncidentDetailResponse;
import com.aquaai.accountability.api.dto.IncidentEvidenceResponse;
import com.aquaai.accountability.api.dto.IncidentResponse;
import com.aquaai.accountability.api.dto.IncidentVerificationResponse;
import com.aquaai.accountability.api.dto.ProposeActionRequest;
import com.aquaai.accountability.api.dto.RecordInvestigationRequest;
import com.aquaai.accountability.api.dto.ReopenIncidentRequest;
import com.aquaai.accountability.api.dto.ReviewIncidentRequest;
import com.aquaai.accountability.api.dto.VerifyResolutionRequest;
import com.aquaai.accountability.application.dto.ApproveActionCommand;
import com.aquaai.accountability.application.dto.AssignInvestigatorCommand;
import com.aquaai.accountability.application.dto.AttachEvidenceCommand;
import com.aquaai.accountability.application.dto.CompleteActionCommand;
import com.aquaai.accountability.application.dto.CreateIncidentCommand;
import com.aquaai.accountability.application.dto.IncidentDetailsDto;
import com.aquaai.accountability.application.dto.ProposeActionCommand;
import com.aquaai.accountability.application.dto.RecordInvestigationCommand;
import com.aquaai.accountability.application.dto.ReopenIncidentCommand;
import com.aquaai.accountability.application.dto.ReviewIncidentCommand;
import com.aquaai.accountability.application.dto.VerifyResolutionCommand;
import com.aquaai.accountability.application.usecase.ApproveCorrectiveActionUseCase;
import com.aquaai.accountability.application.usecase.AssignInvestigatorUseCase;
import com.aquaai.accountability.application.usecase.AttachEvidenceUseCase;
import com.aquaai.accountability.application.usecase.CompleteCorrectiveActionUseCase;
import com.aquaai.accountability.application.usecase.CreateIncidentUseCase;
import com.aquaai.accountability.application.usecase.GetAccountabilityDashboardUseCase;
import com.aquaai.accountability.application.usecase.GetIncidentDetailsUseCase;
import com.aquaai.accountability.application.usecase.GetOverdueIncidentsUseCase;
import com.aquaai.accountability.application.usecase.IngestFindingsUseCase;
import com.aquaai.accountability.application.usecase.ListIncidentsUseCase;
import com.aquaai.accountability.application.usecase.ProposeCorrectiveActionUseCase;
import com.aquaai.accountability.application.usecase.RecordInvestigationFindingsUseCase;
import com.aquaai.accountability.application.usecase.ReopenIncidentUseCase;
import com.aquaai.accountability.application.usecase.ReviewIncidentUseCase;
import com.aquaai.accountability.application.usecase.VerifyResolutionUseCase;
import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.AccountabilityFieldPort;
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
 * REST controller for Milestone 08.16: Regional Water Accountability, Incident Investigation & Corrective Action Governance.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accountability")
@RequiredArgsConstructor
public class AccountabilityController {

    private final CreateIncidentUseCase createIncidentUseCase;
    private final ReviewIncidentUseCase reviewIncidentUseCase;
    private final AssignInvestigatorUseCase assignInvestigatorUseCase;
    private final RecordInvestigationFindingsUseCase recordInvestigationFindingsUseCase;
    private final AttachEvidenceUseCase attachEvidenceUseCase;
    private final ProposeCorrectiveActionUseCase proposeActionUseCase;
    private final ApproveCorrectiveActionUseCase approveActionUseCase;
    private final CompleteCorrectiveActionUseCase completeActionUseCase;
    private final VerifyResolutionUseCase verifyResolutionUseCase;
    private final ReopenIncidentUseCase reopenIncidentUseCase;
    private final GetIncidentDetailsUseCase getIncidentDetailsUseCase;
    private final ListIncidentsUseCase listIncidentsUseCase;
    private final GetAccountabilityDashboardUseCase getDashboardUseCase;
    private final GetOverdueIncidentsUseCase getOverdueIncidentsUseCase;
    private final IngestFindingsUseCase ingestFindingsUseCase;
    private final AccountabilityFieldPort fieldPort;

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> listIncidents(
            @RequestParam(required = false) IncidentCategory category,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) UUID wuaId,
            @RequestParam(required = false) UUID canalId,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) Boolean overdueOnly,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID effectiveFieldId = fieldId;
        if (SecurityContextHelper.hasRole(UserRole.FARMER)) {
            UUID currentUserId = SecurityContextHelper.getCurrentUserId();
            if (fieldId != null) {
                if (!fieldPort.isFieldOwnedBy(fieldId, currentUserId)) {
                    throw new UnauthorizedOperationException("Farmers can only view incidents on their own fields");
                }
            } else {
                throw new UnauthorizedOperationException("Farmers must specify their field ID to view associated incidents");
            }
        }

        var result = listIncidentsUseCase.execute(
                category, severity, status, wuaId, canalId, effectiveFieldId, overdueOnly, startDate, endDate, page, size
        );
        Instant now = Instant.now();
        List<IncidentResponse> responses = result.incidents().stream()
                .map(i -> IncidentResponse.from(i, now))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/incidents/{id}")
    public ResponseEntity<ApiResponse<IncidentDetailResponse>> getIncident(@PathVariable UUID id) {
        IncidentDetailsDto details = getIncidentDetailsUseCase.execute(id);

        if (SecurityContextHelper.hasRole(UserRole.FARMER)) {
            UUID currentUserId = SecurityContextHelper.getCurrentUserId();
            UUID incidentFieldId = details.incident().getFieldId();
            if (incidentFieldId == null || !fieldPort.isFieldOwnedBy(incidentFieldId, currentUserId)) {
                throw new UnauthorizedOperationException("Farmers can only view details of incidents associated with their own fields");
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(IncidentDetailResponse.from(details, Instant.now())));
    }

    @PostMapping("/incidents")
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        if (currentRole == UserRole.FARMER) {
            if (request.fieldId() == null || !fieldPort.isFieldOwnedBy(request.fieldId(), currentUserId)) {
                throw new UnauthorizedOperationException("Farmers can only report incidents for their own fields");
            }
        }

        CreateIncidentCommand cmd = new CreateIncidentCommand(
                request.title(),
                request.description(),
                request.category(),
                request.severity(),
                request.confidence(),
                request.sourceModule() != null ? request.sourceModule() : "MANUAL",
                request.sourceEntityType() != null ? request.sourceEntityType() : "REPORT",
                request.sourceEntityId(),
                request.findingReference(),
                request.wuaId(),
                request.canalId(),
                request.fieldId(),
                request.locationDescription(),
                request.detectionTimestamp() != null ? request.detectionTimestamp() : Instant.now(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        WaterIncident created = createIncidentUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentResponse.from(created, Instant.now())));
    }

    @PostMapping("/incidents/{id}/review")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentResponse>> reviewIncident(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewIncidentRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        ReviewIncidentCommand cmd = new ReviewIncidentCommand(
                id,
                request.accept(),
                request.notes(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        WaterIncident reviewed = reviewIncidentUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentResponse.from(reviewed, Instant.now())));
    }

    @PostMapping("/incidents/{id}/assign")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignInvestigator(
            @PathVariable UUID id,
            @Valid @RequestBody AssignInvestigatorRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        AssignInvestigatorCommand cmd = new AssignInvestigatorCommand(
                id,
                request.investigatorId(),
                request.investigatorName(),
                request.organization(),
                request.reason(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        WaterIncident assigned = assignInvestigatorUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentResponse.from(assigned, Instant.now())));
    }

    @PostMapping("/incidents/{id}/investigations")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentResponse>> recordInvestigation(
            @PathVariable UUID id,
            @Valid @RequestBody RecordInvestigationRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        RecordInvestigationCommand cmd = new RecordInvestigationCommand(
                id,
                request.rootCauseCategory(),
                request.rootCauseDetails(),
                request.updatedConfidence(),
                request.notes(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        WaterIncident updated = recordInvestigationFindingsUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentResponse.from(updated, Instant.now())));
    }

    @PostMapping("/incidents/{id}/evidence")
    public ResponseEntity<ApiResponse<IncidentEvidenceResponse>> attachEvidence(
            @PathVariable UUID id,
            @Valid @RequestBody AttachEvidenceRequest request
    ) {
        IncidentDetailsDto details = getIncidentDetailsUseCase.execute(id);
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        if (currentRole == UserRole.FARMER) {
            UUID fieldId = details.incident().getFieldId();
            if (fieldId == null || !fieldPort.isFieldOwnedBy(fieldId, currentUserId)) {
                throw new UnauthorizedOperationException("Farmers can only attach evidence to their own fields' incidents");
            }
        }

        AttachEvidenceCommand cmd = new AttachEvidenceCommand(
                id,
                request.evidenceType(),
                request.title(),
                request.description(),
                request.evidenceUri(),
                request.confidence(),
                Boolean.TRUE.equals(request.isSimulated()),
                request.sourceReference(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        IncidentEvidence saved = attachEvidenceUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentEvidenceResponse.from(saved)));
    }

    @PostMapping("/incidents/{id}/actions")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CorrectiveActionResponse>> proposeAction(
            @PathVariable UUID id,
            @Valid @RequestBody ProposeActionRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        ProposeActionCommand cmd = new ProposeActionCommand(
                id,
                request.title(),
                request.description(),
                request.objective(),
                request.actionOwnerId(),
                request.actionOwnerName(),
                request.priority(),
                request.dueDate(),
                request.expectedOutcome(),
                request.verificationRequirement(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        CorrectiveAction saved = proposeActionUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(CorrectiveActionResponse.from(saved)));
    }

    @PostMapping("/incidents/{id}/approve-action")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CorrectiveActionResponse>> approveAction(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveActionRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        ApproveActionCommand cmd = new ApproveActionCommand(
                id,
                request.actionId(),
                request.notes(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        CorrectiveAction approved = approveActionUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(CorrectiveActionResponse.from(approved)));
    }

    @PostMapping("/incidents/{id}/complete-action")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CorrectiveActionResponse>> completeAction(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteActionRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        CompleteActionCommand cmd = new CompleteActionCommand(
                id,
                request.actionId(),
                request.completionNotes(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        CorrectiveAction completed = completeActionUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(CorrectiveActionResponse.from(completed)));
    }

    @PostMapping("/incidents/{id}/verify")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentVerificationResponse>> verifyIncident(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyResolutionRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        // Enforce separation of duties: verifier is the authenticated user
        VerifyResolutionCommand cmd = new VerifyResolutionCommand(
                id,
                request.actionId(),
                request.verificationMethod(),
                request.result(),
                request.explanatoryNotes(),
                request.supportingEvidenceUri(),
                Boolean.TRUE.equals(request.administrativeOverride()),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        IncidentVerification verification = verifyResolutionUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentVerificationResponse.from(verification)));
    }

    @PostMapping("/incidents/{id}/reopen")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentResponse>> reopenIncident(
            @PathVariable UUID id,
            @Valid @RequestBody ReopenIncidentRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentUserId();
        UserRole currentRole = SecurityContextHelper.getCurrentUserRole();

        ReopenIncidentCommand cmd = new ReopenIncidentCommand(
                id,
                request.reason(),
                currentUserId,
                SecurityContextHelper.getRequiredPrincipal().getUsername(),
                currentRole.name()
        );

        WaterIncident reopened = reopenIncidentUseCase.execute(cmd);
        return ResponseEntity.ok(ApiResponse.ok(IncidentResponse.from(reopened, Instant.now())));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AccountabilityDashboardResponse>> getDashboard() {
        var summary = getDashboardUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(AccountabilityDashboardResponse.from(summary)));
    }

    @GetMapping("/sla/overdue")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getOverdueIncidents() {
        Instant now = Instant.now();
        List<IncidentResponse> list = getOverdueIncidentsUseCase.execute().stream()
                .map(i -> IncidentResponse.from(i, now))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/ingest-findings")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> ingestFindings() {
        Instant now = Instant.now();
        List<IncidentResponse> ingested = ingestFindingsUseCase.execute().stream()
                .map(i -> IncidentResponse.from(i, now))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(ingested));
    }
}
