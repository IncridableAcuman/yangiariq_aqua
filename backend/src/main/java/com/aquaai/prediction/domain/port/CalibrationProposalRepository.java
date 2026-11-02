package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalibrationProposalRepository {
    Optional<CalibrationProposal> findById(UUID id);
    Optional<CalibrationProposal> findByProposalCode(String proposalCode);
    List<CalibrationProposal> findAll();
    List<CalibrationProposal> findOpenProposals();
    List<CalibrationProposal> findByPredictionType(PredictionType type);
    CalibrationProposal save(CalibrationProposal proposal);
}
