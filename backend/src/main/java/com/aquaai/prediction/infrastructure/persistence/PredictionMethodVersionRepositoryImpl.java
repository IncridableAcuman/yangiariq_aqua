package com.aquaai.prediction.infrastructure.persistence;

import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PredictionMethodVersionRepositoryImpl implements PredictionMethodVersionRepository {

    private final SpringDataPredictionMethodVersionRepository springDataRepository;
    private final PredictionGovernanceJpaMapper mapper;

    @Override
    public Optional<PredictionMethodVersion> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PredictionMethodVersion> findByVersionTag(String versionTag) {
        return springDataRepository.findByVersionTag(versionTag).map(mapper::toDomain);
    }

    @Override
    public Optional<PredictionMethodVersion> findActiveByPredictionType(PredictionType type) {
        return springDataRepository.findByPredictionTypeAndStatus(type.name(), "ACTIVE")
                .map(mapper::toDomain);
    }

    @Override
    public List<PredictionMethodVersion> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionMethodVersion> findByPredictionType(PredictionType type) {
        return springDataRepository.findByPredictionType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PredictionMethodVersion save(PredictionMethodVersion version) {
        PredictionMethodVersionJpaEntity entity = mapper.toEntity(version);
        PredictionMethodVersionJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
