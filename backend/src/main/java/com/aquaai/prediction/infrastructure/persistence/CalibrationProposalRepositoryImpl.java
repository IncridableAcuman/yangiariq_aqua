package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.CalibrationProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CalibrationProposalRepositoryImpl implements CalibrationProposalRepository {

    private final SpringDataCalibrationProposalRepository springDataRepository;
    private final PredictionGovernanceJpaMapper mapper;

    @Override
    public Optional<CalibrationProposal> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CalibrationProposal> findByProposalCode(String proposalCode) {
        return springDataRepository.findByProposalCode(proposalCode).map(mapper::toDomain);
    }

    @Override
    public List<CalibrationProposal> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CalibrationProposal> findOpenProposals() {
        return springDataRepository.findOpenProposals().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CalibrationProposal> findByPredictionType(PredictionType type) {
        return springDataRepository.findByPredictionType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public CalibrationProposal save(CalibrationProposal proposal) {
        CalibrationProposalJpaEntity entity = mapper.toEntity(proposal);
        CalibrationProposalJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
