package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.OperatorFeedback;
import com.aquaai.pilot.domain.model.OperatorFeedbackCategory;
import com.aquaai.pilot.domain.model.OperatorFeedbackStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperatorFeedbackRepository {
    OperatorFeedback save(OperatorFeedback feedback);
    Optional<OperatorFeedback> findById(UUID id);
    List<OperatorFeedback> findByPilotId(UUID pilotId);
    List<OperatorFeedback> findByPilotIdAndStatus(UUID pilotId, OperatorFeedbackStatus status);
    List<OperatorFeedback> findByPilotIdAndCategory(UUID pilotId, OperatorFeedbackCategory category);
    long countByPilotId(UUID pilotId);
    long countByPilotIdAndStatus(UUID pilotId, OperatorFeedbackStatus status);
}
