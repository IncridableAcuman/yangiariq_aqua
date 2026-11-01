package com.aquaai.anomaly.infrastructure.persistence;

import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.SensorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAnomalyRepository extends JpaRepository<AnomalyJpaEntity, UUID> {

    @Query("SELECT a FROM AnomalyJpaEntity a WHERE a.fieldId = :fieldId " +
            "AND ((:sensorId IS NULL AND a.sensorId IS NULL) OR a.sensorId = :sensorId) " +
            "AND a.anomalyType = :anomalyType " +
            "AND a.status IN ('DETECTED', 'ACKNOWLEDGED', 'INVESTIGATING')")
    Optional<AnomalyJpaEntity> findActiveAnomaly(
            @Param("fieldId") UUID fieldId,
            @Param("sensorId") UUID sensorId,
            @Param("anomalyType") AnomalyType anomalyType
    );

    @Query("SELECT a FROM AnomalyJpaEntity a WHERE " +
            "(:fieldId IS NULL OR a.fieldId = :fieldId) AND " +
            "(:sensorId IS NULL OR a.sensorId = :sensorId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:severity IS NULL OR a.severity = :severity) AND " +
            "(:anomalyType IS NULL OR a.anomalyType = :anomalyType) AND " +
            "(:sensorType IS NULL OR a.sensorType = :sensorType) AND " +
            "(cast(:from as timestamp) IS NULL OR a.detectedAt >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR a.detectedAt <= :to)")
    Page<AnomalyJpaEntity> findWithFilters(
            @Param("fieldId") UUID fieldId,
            @Param("sensorId") UUID sensorId,
            @Param("status") AnomalyStatus status,
            @Param("severity") AnomalySeverity severity,
            @Param("anomalyType") AnomalyType anomalyType,
            @Param("sensorType") SensorType sensorType,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM AnomalyJpaEntity a WHERE a.fieldId = :fieldId AND a.status IN ('DETECTED', 'ACKNOWLEDGED', 'INVESTIGATING')")
    long countActiveByFieldId(@Param("fieldId") UUID fieldId);

    @Query("SELECT a FROM AnomalyJpaEntity a WHERE a.fieldId IN :fieldIds " +
            "AND a.status IN ('DETECTED', 'ACKNOWLEDGED', 'INVESTIGATING') " +
            "ORDER BY a.detectedAt DESC")
    java.util.List<AnomalyJpaEntity> findActiveByFieldIdIn(@Param("fieldIds") java.util.Collection<UUID> fieldIds);

    long countByStatus(AnomalyStatus status);

    long countBySeverity(AnomalySeverity severity);
}
