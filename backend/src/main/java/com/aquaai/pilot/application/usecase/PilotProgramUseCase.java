package com.aquaai.pilot.application.usecase;

import com.aquaai.pilot.application.dto.PilotDtos.*;
import com.aquaai.pilot.domain.model.*;
import com.aquaai.pilot.domain.repository.PilotFieldRepository;
import com.aquaai.pilot.domain.repository.PilotParticipantRepository;
import com.aquaai.pilot.domain.repository.PilotProgramRepository;
import com.aquaai.pilot.domain.repository.PilotReadinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class PilotProgramUseCase {

    private final PilotProgramRepository pilotRepository;
    private final PilotParticipantRepository participantRepository;
    private final PilotFieldRepository fieldRepository;
    private final PilotReadinessRepository readinessRepository;

    public PilotProgramUseCase(
            PilotProgramRepository pilotRepository,
            PilotParticipantRepository participantRepository,
            PilotFieldRepository fieldRepository,
            PilotReadinessRepository readinessRepository) {
        this.pilotRepository = pilotRepository;
        this.participantRepository = participantRepository;
        this.fieldRepository = fieldRepository;
        this.readinessRepository = readinessRepository;
    }

    public PilotProgramResponse createPilot(CreatePilotRequest req, UUID ownerId) {
        if (pilotRepository.existsByCode(req.code())) {
            throw new IllegalArgumentException("Pilot program code already exists: " + req.code());
        }
        var program = PilotProgram.create(
                req.code(),
                req.name(),
                req.description(),
                req.geographicScope(),
                req.startDate(),
                req.endDate(),
                req.organization(),
                ownerId,
                req.operationalObjectives()
        );
        var saved = pilotRepository.save(program);
        return toDto(saved);
    }

    public PilotProgramResponse updatePilot(UUID id, UpdatePilotRequest req) {
        var program = getProgram(id);
        program.update(
                req.name(),
                req.description(),
                req.geographicScope(),
                req.startDate(),
                req.endDate(),
                req.organization(),
                req.operationalObjectives()
        );
        return toDto(pilotRepository.save(program));
    }

    public PilotProgramResponse approvePilot(UUID id, ApprovePilotRequest req, UUID approverId) {
        var program = getProgram(id);
        program.approve(approverId, req.notes());
        return toDto(pilotRepository.save(program));
    }

    public PilotProgramResponse activatePilot(UUID id) {
        var program = getProgram(id);
        var latestReadiness = readinessRepository.findLatestByPilotId(id);
        if (latestReadiness.isEmpty()) {
            throw new IllegalStateException("Cannot activate pilot: Pre-flight readiness assessment must be performed prior to activation.");
        }
        if (latestReadiness.get().getOverallStatus() == ReadinessResult.BLOCKED) {
            throw new IllegalStateException("Pilot activation blocked: One or more mandatory readiness requirements are BLOCKED.");
        }
        program.activate();
        return toDto(pilotRepository.save(program));
    }

    public PilotProgramResponse pausePilot(UUID id) {
        var program = getProgram(id);
        program.pause();
        return toDto(pilotRepository.save(program));
    }

    public PilotProgramResponse completePilot(UUID id) {
        var program = getProgram(id);
        program.complete();
        return toDto(pilotRepository.save(program));
    }

    public PilotProgramResponse cancelPilot(UUID id, CancelPilotRequest req) {
        var program = getProgram(id);
        program.cancel(req != null ? req.reason() : null);
        return toDto(pilotRepository.save(program));
    }

    @Transactional(readOnly = true)
    public PilotProgramResponse getPilot(UUID id) {
        return toDto(getProgram(id));
    }

    @Transactional(readOnly = true)
    public List<PilotProgramResponse> listPilots(PilotStatus status) {
        List<PilotProgram> list = (status != null) ? pilotRepository.findByStatus(status) : pilotRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    // Participants
    public PilotParticipantResponse addParticipant(UUID pilotId, AddParticipantRequest req) {
        getProgram(pilotId); // verify exists
        var existing = participantRepository.findByPilotIdAndUserId(pilotId, req.userId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this pilot program");
        }
        var participant = PilotParticipant.create(pilotId, req.userId(), req.roleInPilot(), req.assignedFieldIds());
        var saved = participantRepository.save(participant);
        return toParticipantDto(saved);
    }

    public void removeParticipant(UUID pilotId, UUID participantId) {
        participantRepository.delete(participantId);
    }

    @Transactional(readOnly = true)
    public List<PilotParticipantResponse> listParticipants(UUID pilotId) {
        return participantRepository.findByPilotId(pilotId).stream().map(this::toParticipantDto).toList();
    }

    // Fields
    public PilotFieldResponse addField(UUID pilotId, AddFieldRequest req) {
        getProgram(pilotId); // verify exists
        var existing = fieldRepository.findByPilotIdAndFieldId(pilotId, req.fieldId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Field parcel is already enrolled in this pilot program");
        }
        var field = PilotField.create(pilotId, req.fieldId(), req.monitoringTarget(), req.measurementPlan(), req.validationCriteria());
        var saved = fieldRepository.save(field);
        return toFieldDto(saved);
    }

    public void removeField(UUID pilotId, UUID fieldId) {
        fieldRepository.delete(fieldId);
    }

    @Transactional(readOnly = true)
    public List<PilotFieldResponse> listFields(UUID pilotId) {
        return fieldRepository.findByPilotId(pilotId).stream().map(this::toFieldDto).toList();
    }

    private PilotProgram getProgram(UUID id) {
        return pilotRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pilot program not found: " + id));
    }

    private PilotProgramResponse toDto(PilotProgram p) {
        return new PilotProgramResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getDescription(),
                p.getGeographicScope(),
                p.getStartDate(),
                p.getEndDate(),
                p.getOrganization(),
                p.getOwnerId(),
                p.getOperationalObjectives(),
                p.getStatus().name(),
                p.getApprovedBy(),
                p.getApprovedAt(),
                p.getApprovalNotes(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private PilotParticipantResponse toParticipantDto(PilotParticipant p) {
        return new PilotParticipantResponse(
                p.getId(),
                p.getPilotId(),
                p.getUserId(),
                p.getRoleInPilot(),
                p.getAssignedFieldIds(),
                p.getJoinedAt()
        );
    }

    private PilotFieldResponse toFieldDto(PilotField f) {
        return new PilotFieldResponse(
                f.getId(),
                f.getPilotId(),
                f.getFieldId(),
                f.getMonitoringTarget(),
                f.getMeasurementPlan(),
                f.getValidationCriteria(),
                f.getAddedAt()
        );
    }
}
