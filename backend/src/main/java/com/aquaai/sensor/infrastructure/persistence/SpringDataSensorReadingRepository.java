package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSensorReadingRepository extends JpaRepository<SensorReadingJpaEntity, UUID> {

    Optional<SensorReadingJpaEntity> findFirstBySensorIdOrderByMeasuredAtDesc(UUID sensorId);

    Page<SensorReadingJpaEntity> findBySensorIdAndMeasuredAtBetween(
            UUID sensorId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    Page<SensorReadingJpaEntity> findByFieldIdAndMeasuredAtBetween(
            UUID fieldId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    @Query("SELECT r FROM SensorReadingJpaEntity r " +
            "WHERE r.fieldId = :fieldId " +
            "AND r.measuredAt = (" +
            "   SELECT MAX(r2.measuredAt) FROM SensorReadingJpaEntity r2 " +
            "   WHERE r2.fieldId = :fieldId AND r2.type = r.type" +
            ") " +
            "ORDER BY r.type ASC")
    List<SensorReadingJpaEntity> findLatestByFieldIdGroupedByType(@Param("fieldId") UUID fieldId);

    @Query("SELECT r FROM SensorReadingJpaEntity r " +
            "WHERE r.fieldId IN :fieldIds AND r.type = 'SOIL_MOISTURE' " +
            "AND r.measuredAt = (" +
            "   SELECT MAX(r2.measuredAt) FROM SensorReadingJpaEntity r2 " +
            "   WHERE r2.fieldId = r.fieldId AND r2.type = 'SOIL_MOISTURE'" +
            ")")
    List<SensorReadingJpaEntity> findLatestSoilMoistureByFieldIdIn(@Param("fieldIds") java.util.Collection<UUID> fieldIds);

    @Query("SELECT r.id AS id, r.sensorId AS sensorId, r.fieldId AS fieldId, r.type AS type, " +
            "r.value AS value, r.unit AS unit, r.quality AS quality, r.source AS source, r.measuredAt AS measuredAt " +
            "FROM SensorReadingJpaEntity r " +
            "WHERE r.sensorId = :sensorId " +
            "  AND r.measuredAt >= :from AND r.measuredAt <= :to " +
            "  AND (:quality IS NULL OR r.quality = :quality) " +
            "ORDER BY r.measuredAt ASC")
    List<SensorReadingPointProjection> findReadingsForSensorAnalytics(
            @Param("sensorId") UUID sensorId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("quality") ReadingQuality quality
    );

    @Query("SELECT r.id AS id, r.sensorId AS sensorId, r.fieldId AS fieldId, r.type AS type, " +
            "r.value AS value, r.unit AS unit, r.quality AS quality, r.source AS source, r.measuredAt AS measuredAt " +
            "FROM SensorReadingJpaEntity r " +
            "WHERE r.fieldId = :fieldId " +
            "  AND (:type IS NULL OR r.type = :type) " +
            "  AND r.measuredAt >= :from AND r.measuredAt <= :to " +
            "  AND (:quality IS NULL OR r.quality = :quality) " +
            "ORDER BY r.measuredAt ASC")
    List<SensorReadingPointProjection> findReadingsForFieldAnalytics(
            @Param("fieldId") UUID fieldId,
            @Param("type") SensorType type,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("quality") ReadingQuality quality
    );

    @Query("SELECT r.quality, COUNT(r) " +
            "FROM SensorReadingJpaEntity r " +
            "WHERE (:sensorId IS NULL OR r.sensorId = :sensorId) " +
            "  AND (:fieldId IS NULL OR r.fieldId = :fieldId) " +
            "  AND r.measuredAt >= :from AND r.measuredAt <= :to " +
            "GROUP BY r.quality")
    List<Object[]> countByQualityBetween(
            @Param("sensorId") UUID sensorId,
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM SensorReadingJpaEntity r WHERE r.measuredAt < :cutoff")
    int deleteByMeasuredAtBefore(@Param("cutoff") Instant cutoff);
}
