package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.repository.ResilienceScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ResilienceScenarioRepositoryImpl implements ResilienceScenarioRepository {

    private final SpringDataResilienceScenarioRepository springDataRepository;
    private final ResilienceScenarioJpaMapper mapper;

    @Override
    public ResilienceScenario save(ResilienceScenario scenario) {
        ResilienceScenarioJpaEntity entity = mapper.toJpaEntity(scenario);
        ResilienceScenarioJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ResilienceScenario> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ResilienceScenario> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
