package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.sensor.domain.model.SensorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AnomalyRuleConfigRepositoryImpl implements AnomalyRuleConfigRepository {

    private final SpringDataAnomalyRuleConfigRepository springDataRepository;
    private final AnomalyJpaMapper mapper;

    @Override
    public AnomalyRuleConfig save(AnomalyRuleConfig config) {
        AnomalyRuleConfigJpaEntity entity = mapper.toEntity(config);
        AnomalyRuleConfigJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AnomalyRuleConfig> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AnomalyRuleConfig> findByRuleCode(String ruleCode) {
        return springDataRepository.findByRuleCode(ruleCode).map(mapper::toDomain);
    }

    @Override
    public List<AnomalyRuleConfig> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AnomalyRuleConfig> findBySensorType(SensorType sensorType) {
        return springDataRepository.findBySensorType(sensorType).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
