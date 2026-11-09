package com.aquaai.pilot.application.usecase;

import com.aquaai.pilot.application.dto.PilotDtos.OperatorFeedbackResponse;
import com.aquaai.pilot.application.dto.PilotDtos.SubmitFeedbackRequest;
import com.aquaai.pilot.application.dto.PilotDtos.UpdateFeedbackStatusRequest;
import com.aquaai.pilot.domain.model.OperatorFeedback;
import com.aquaai.pilot.domain.model.OperatorFeedbackCategory;
import com.aquaai.pilot.domain.model.OperatorFeedbackStatus;
import com.aquaai.pilot.domain.repository.OperatorFeedbackRepository;
import com.aquaai.pilot.domain.repository.PilotProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class OperatorFeedbackUseCase {

    private final OperatorFeedbackRepository feedbackRepository;
    private final PilotProgramRepository pilotProgramRepository;

    public OperatorFeedbackUseCase(
            OperatorFeedbackRepository feedbackRepository,
            PilotProgramRepository pilotProgramRepository) {
        this.feedbackRepository = feedbackRepository;
        this.pilotProgramRepository = pilotProgramRepository;
    }

    public OperatorFeedbackResponse submitFeedback(UUID pilotId, SubmitFeedbackRequest req, UUID submitterId) {
        pilotProgramRepository.findById(pilotId)
                .orElseThrow(() -> new NoSuchElementException("Pilot program not found: " + pilotId));

        OperatorFeedbackCategory cat;
        try {
            cat = OperatorFeedbackCategory.valueOf(req.category().toUpperCase());
        } catch (Exception e) {
            cat = OperatorFeedbackCategory.OTHER;
        }

        var feedback = OperatorFeedback.submit(
                pilotId,
                req.farmId(),
                req.fieldId(),
                req.sensorId(),
                req.dataSourceId(),
                req.incidentId(),
                cat,
                req.title(),
                req.description(),
                submitterId
        );

        var saved = feedbackRepository.save(feedback);
        return toDto(saved);
    }

    public OperatorFeedbackResponse updateStatus(UUID feedbackId, UpdateFeedbackStatusRequest req) {
        var feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NoSuchElementException("Operator feedback record not found: " + feedbackId));

        OperatorFeedbackStatus status = OperatorFeedbackStatus.valueOf(req.status().toUpperCase());
        feedback.updateStatus(status, req.assignedTo(), req.resolutionNotes());

        var saved = feedbackRepository.save(feedback);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public OperatorFeedbackResponse getFeedback(UUID id) {
        return feedbackRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Operator feedback record not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<OperatorFeedbackResponse> listFeedback(UUID pilotId, OperatorFeedbackStatus status, OperatorFeedbackCategory category) {
        List<OperatorFeedback> list;
        if (status != null) {
            list = feedbackRepository.findByPilotIdAndStatus(pilotId, status);
        } else if (category != null) {
            list = feedbackRepository.findByPilotIdAndCategory(pilotId, category);
        } else {
            list = feedbackRepository.findByPilotId(pilotId);
        }
        return list.stream().map(this::toDto).toList();
    }

    private OperatorFeedbackResponse toDto(OperatorFeedback f) {
        return new OperatorFeedbackResponse(
                f.getId(),
                f.getPilotId(),
                f.getFarmId(),
                f.getFieldId(),
                f.getSensorId(),
                f.getDataSourceId(),
                f.getIncidentId(),
                f.getCategory().name(),
                f.getTitle(),
                f.getDescription(),
                f.getStatus().name(),
                f.getSubmittedBy(),
                f.getAssignedTo(),
                f.getResolutionNotes(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
