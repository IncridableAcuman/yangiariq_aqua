package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.sensor.domain.model.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAnomalyRuleConfigRepository extends JpaRepository<AnomalyRuleConfigJpaEntity, UUID> {

    Optional<AnomalyRuleConfigJpaEntity> findByRuleCode(String ruleCode);

    List<AnomalyRuleConfigJpaEntity> findBySensorType(SensorType sensorType);
}
