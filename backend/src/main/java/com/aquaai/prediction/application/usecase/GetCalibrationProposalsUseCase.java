package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.CalibrationProposalDto;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCalibrationProposalsUseCase {

    private final CalibrationProposalRepository proposalRepository;

    @Transactional(readOnly = true)
    public List<CalibrationProposalDto> execute(PredictionType type, boolean openOnly) {
        if (openOnly) {
            return proposalRepository.findOpenProposals().stream()
                    .filter(p -> type == null || p.getPredictionType() == type)
                    .map(CalibrationProposalDto::fromDomain)
                    .collect(Collectors.toList());
        }
        if (type != null) {
            return proposalRepository.findByPredictionType(type).stream()
                    .map(CalibrationProposalDto::fromDomain)
                    .collect(Collectors.toList());
        }
        return proposalRepository.findAll().stream()
                .map(CalibrationProposalDto::fromDomain)
                .collect(Collectors.toList());
    }
}
