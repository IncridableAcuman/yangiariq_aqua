package com.aquaai.pilot.application.usecase;

import com.aquaai.integration.domain.service.CanonicalUnitConverter;
import com.aquaai.pilot.application.dto.PilotDtos.GroundTruthResponse;
import com.aquaai.pilot.application.dto.PilotDtos.SubmitGroundTruthRequest;
import com.aquaai.pilot.application.dto.PilotDtos.VerifyGroundTruthRequest;
import com.aquaai.pilot.domain.model.GroundTruthMeasurement;
import com.aquaai.pilot.domain.model.GroundTruthStatus;
import com.aquaai.pilot.domain.model.PilotStatus;
import com.aquaai.pilot.domain.repository.GroundTruthValidationRepository;
import com.aquaai.pilot.domain.repository.PilotProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class GroundTruthUseCase {

    private final GroundTruthValidationRepository groundTruthRepository;
    private final PilotProgramRepository pilotProgramRepository;
    private final CanonicalUnitConverter unitConverter = new CanonicalUnitConverter();

    public GroundTruthUseCase(
            GroundTruthValidationRepository groundTruthRepository,
            PilotProgramRepository pilotProgramRepository) {
        this.groundTruthRepository = groundTruthRepository;
        this.pilotProgramRepository = pilotProgramRepository;
    }

    public GroundTruthResponse submitMeasurement(UUID pilotId, SubmitGroundTruthRequest req, UUID submitterId) {
        var pilot = pilotProgramRepository.findById(pilotId)
                .orElseThrow(() -> new NoSuchElementException("Pilot program not found: " + pilotId));

        if (pilot.getStatus() != PilotStatus.ACTIVE && pilot.getStatus() != PilotStatus.READY) {
            throw new IllegalStateException("Cannot submit ground truth: Pilot must be READY or ACTIVE. Current: " + pilot.getStatus());
        }

        // Canonical unit conversion
        com.aquaai.sensor.domain.model.SensorType sensorType;
        String typeUpper = req.measurementType().toUpperCase();
        if (typeUpper.equals("WATER_FLOW_RATE") || typeUpper.equals("WATER_FLOW")) {
            sensorType = com.aquaai.sensor.domain.model.SensorType.WATER_FLOW;
        } else if (typeUpper.equals("WATER_PRESSURE") || typeUpper.equals("PRESSURE")) {
            sensorType = com.aquaai.sensor.domain.model.SensorType.PRESSURE;
        } else {
            try {
                sensorType = com.aquaai.sensor.domain.model.SensorType.valueOf(typeUpper);
            } catch (Exception e) {
                sensorType = com.aquaai.sensor.domain.model.SensorType.SOIL_MOISTURE;
            }
        }

        java.math.BigDecimal rawVal = java.math.BigDecimal.valueOf(req.measuredValue());
        var conversion = unitConverter.convertToCanonical(sensorType, rawVal, req.canonicalUnit());
        Double normalizedVal = conversion.normalizedValue().doubleValue();
        String canonicalUnit = conversion.canonicalUnit().getSymbol();

        Instant measuredAt = req.measuredAt() != null ? req.measuredAt() : Instant.now();

        var measurement = GroundTruthMeasurement.submit(
                pilotId,
                req.fieldId(),
                req.measurementType(),
                normalizedVal,
                canonicalUnit,
                measuredAt,
                req.collectionMethod(),
                req.measurementSource(),
                submitterId,
                req.evidenceReference(),
                req.notes()
        );

        var saved = groundTruthRepository.save(measurement);
        return toDto(saved);
    }

    public GroundTruthResponse reviewMeasurement(UUID measurementId, VerifyGroundTruthRequest req, UUID verifierId) {
        var measurement = groundTruthRepository.findById(measurementId)
                .orElseThrow(() -> new NoSuchElementException("Ground truth record not found: " + measurementId));

        String action = req.action() != null ? req.action().toUpperCase() : "VERIFY";

        switch (action) {
            case "VERIFY" -> measurement.verify(verifierId, req.evidenceReference(), req.notes());
            case "REJECT" -> measurement.reject(verifierId, req.rejectionReason());
            case "REQUEST_REMEASUREMENT" -> measurement.requestRemeasurement(verifierId, req.rejectionReason());
            default -> throw new IllegalArgumentException("Unknown verification action: " + action + ". Use VERIFY, REJECT, or REQUEST_REMEASUREMENT.");
        }

        var saved = groundTruthRepository.save(measurement);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public GroundTruthResponse getMeasurement(UUID id) {
        return groundTruthRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Ground truth record not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<GroundTruthResponse> listMeasurements(UUID pilotId, GroundTruthStatus status) {
        List<GroundTruthMeasurement> list = (status != null)
                ? groundTruthRepository.findByPilotIdAndStatus(pilotId, status)
                : groundTruthRepository.findByPilotId(pilotId);
        return list.stream().map(this::toDto).toList();
    }

    private GroundTruthResponse toDto(GroundTruthMeasurement m) {
        return new GroundTruthResponse(
                m.getId(),
                m.getPilotId(),
                m.getFieldId(),
                m.getMeasurementType(),
                m.getMeasuredValue(),
                m.getCanonicalUnit(),
                m.getMeasuredAt(),
                m.getSubmittedAt(),
                m.getCollectionMethod(),
                m.getMeasurementSource(),
                m.getSubmittedBy(),
                m.getVerificationStatus().name(),
                m.getVerifierId(),
                m.getEvidenceReference(),
                m.getNotes(),
                m.getVerifiedAt(),
                m.getRejectionReason()
        );
    }
}
