package com.aquaai.anomaly.domain.repository;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.sensor.domain.model.SensorType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for anomaly rule configurations.
 */
public interface AnomalyRuleConfigRepository {

    AnomalyRuleConfig save(AnomalyRuleConfig config);

    Optional<AnomalyRuleConfig> findById(UUID id);

    Optional<AnomalyRuleConfig> findByRuleCode(String ruleCode);

    List<AnomalyRuleConfig> findAll();

    List<AnomalyRuleConfig> findBySensorType(SensorType sensorType);
}
