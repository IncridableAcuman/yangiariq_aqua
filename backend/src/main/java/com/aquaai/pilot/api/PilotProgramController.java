package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.*;
import com.aquaai.pilot.application.usecase.PilotProgramUseCase;
import com.aquaai.pilot.domain.model.PilotStatus;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pilot-programs")
@Tag(name = "Pilot Programs", description = "Endpoints for managing controlled field pilot lifecycle, participants, and fields")
@SecurityRequirement(name = "bearerAuth")
public class PilotProgramController {

    private final PilotProgramUseCase pilotProgramUseCase;

    public PilotProgramController(PilotProgramUseCase pilotProgramUseCase) {
        this.pilotProgramUseCase = pilotProgramUseCase;
    }

    @GetMapping
    @Operation(summary = "List pilot programs", description = "Returns all pilot programs, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<PilotProgramResponse>>> listPilots(
            @RequestParam(required = false) PilotStatus status
    ) {
        List<PilotProgramResponse> list = pilotProgramUseCase.listPilots(status);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST')")
    @Operation(summary = "Create pilot program", description = "Creates a new pilot program in DRAFT status")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> createPilot(
            @RequestBody CreatePilotRequest request
    ) {
        UUID userId = SecurityContextHelper.getCurrentUserId();
        PilotProgramResponse created = pilotProgramUseCase.createPilot(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pilot program details", description = "Retrieves full details of a pilot program")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> getPilot(@PathVariable UUID id) {
        PilotProgramResponse response = pilotProgramUseCase.getPilot(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Update pilot program", description = "Updates pilot program metadata")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> updatePilot(
            @PathVariable UUID id,
            @RequestBody UpdatePilotRequest request
    ) {
        PilotProgramResponse updated = pilotProgramUseCase.updatePilot(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Approve pilot program", description = "Approves a DRAFT or PLANNED pilot program")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> approvePilot(
            @PathVariable UUID id,
            @RequestBody ApprovePilotRequest request
    ) {
        UUID approverId = SecurityContextHelper.getCurrentUserId();
        PilotProgramResponse approved = pilotProgramUseCase.approvePilot(id, request, approverId);
        return ResponseEntity.ok(ApiResponse.ok(approved));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Activate pilot program", description = "Activates an approved/ready pilot program after verifying readiness")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> activatePilot(@PathVariable UUID id) {
        PilotProgramResponse activated = pilotProgramUseCase.activatePilot(id);
        return ResponseEntity.ok(ApiResponse.ok(activated));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Pause active pilot program", description = "Pauses an active pilot program")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> pausePilot(@PathVariable UUID id) {
        PilotProgramResponse paused = pilotProgramUseCase.pausePilot(id);
        return ResponseEntity.ok(ApiResponse.ok(paused));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Complete pilot program", description = "Transitions an active or paused pilot to COMPLETED")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> completePilot(@PathVariable UUID id) {
        PilotProgramResponse completed = pilotProgramUseCase.completePilot(id);
        return ResponseEntity.ok(ApiResponse.ok(completed));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Cancel pilot program", description = "Cancels a pilot program with a reason")
    public ResponseEntity<ApiResponse<PilotProgramResponse>> cancelPilot(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelPilotRequest request
    ) {
        PilotProgramResponse cancelled = pilotProgramUseCase.cancelPilot(id, request);
        return ResponseEntity.ok(ApiResponse.ok(cancelled));
    }

    // Participants
    @GetMapping("/{id}/participants")
    @Operation(summary = "List enrolled participants", description = "Returns all participants in this pilot")
    public ResponseEntity<ApiResponse<List<PilotParticipantResponse>>> listParticipants(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(pilotProgramUseCase.listParticipants(id)));
    }

    @PostMapping("/{id}/participants")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Enroll participant in pilot", description = "Enrolls an operator or farmer in the pilot")
    public ResponseEntity<ApiResponse<PilotParticipantResponse>> addParticipant(
            @PathVariable UUID id,
            @RequestBody AddParticipantRequest request
    ) {
        PilotParticipantResponse added = pilotProgramUseCase.addParticipant(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(added));
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Remove participant from pilot", description = "Disenrolls a participant from the pilot")
    public ResponseEntity<ApiResponse<Void>> removeParticipant(
            @PathVariable UUID id,
            @PathVariable UUID participantId
    ) {
        pilotProgramUseCase.removeParticipant(id, participantId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // Fields
    @GetMapping("/{id}/fields")
    @Operation(summary = "List enrolled fields", description = "Returns all agricultural parcels monitored in this pilot")
    public ResponseEntity<ApiResponse<List<PilotFieldResponse>>> listFields(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(pilotProgramUseCase.listFields(id)));
    }

    @PostMapping("/{id}/fields")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Enroll field parcel in pilot", description = "Enrolls a parcel into the pilot with monitoring plan")
    public ResponseEntity<ApiResponse<PilotFieldResponse>> addField(
            @PathVariable UUID id,
            @RequestBody AddFieldRequest request
    ) {
        PilotFieldResponse added = pilotProgramUseCase.addField(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(added));
    }

    @DeleteMapping("/{id}/fields/{fieldId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Remove field parcel from pilot", description = "Disenrolls a parcel from the pilot")
    public ResponseEntity<ApiResponse<Void>> removeField(
            @PathVariable UUID id,
            @PathVariable UUID fieldId
    ) {
        pilotProgramUseCase.removeField(id, fieldId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
