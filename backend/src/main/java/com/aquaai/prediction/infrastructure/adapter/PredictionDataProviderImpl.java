package com.aquaai.prediction.infrastructure.adapter;

import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.aquaai.recommendation.domain.port.EvapotranspirationCalculator;
import com.aquaai.recommendation.domain.port.WeatherDataProvider;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionDataProviderImpl implements PredictionDataProvider {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final WaterConsumptionQueryRepository waterConsumptionQueryRepository;
    private final WeatherDataProvider weatherDataProvider;
    private final EvapotranspirationCalculator evapotranspirationCalculator;

    @Override
    public List<FieldMetadata> getAccessibleFields(UUID userId, boolean isAdminOrOperator) {
        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (isAdminOrOperator) {
            sql = "SELECT id, owner_id, name, crop_type, soil_type, irrigation_method, area_hectares, status FROM fields WHERE status = 'ACTIVE' ORDER BY name ASC";
        } else {
            sql = "SELECT id, owner_id, name, crop_type, soil_type, irrigation_method, area_hectares, status FROM fields WHERE owner_id = :userId AND status = 'ACTIVE' ORDER BY name ASC";
            params.addValue("userId", userId);
        }

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapFieldMetadata(rs));
    }

    @Override
    public Optional<FieldMetadata> getFieldMetadata(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        String sql = "SELECT id, owner_id, name, crop_type, soil_type, irrigation_method, area_hectares, status FROM fields WHERE id = :fieldId";
        MapSqlParameterSource params = new MapSqlParameterSource("fieldId", fieldId);
        List<FieldMetadata> list = jdbcTemplate.query(sql, params, (rs, rowNum) -> mapFieldMetadata(rs));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<TelemetryObservation> getRecentMoistureObservations(UUID fieldId, Instant since) {
        if (fieldId == null) return Collections.emptyList();
        String sql = """
            SELECT r.sensor_id, r.type, r."value", r.quality, r.measured_at
            FROM sensor_readings r
            WHERE r.field_id = :fieldId
              AND r.type = 'SOIL_MOISTURE'
              AND (cast(:since as timestamp) IS NULL OR r.measured_at >= :since)
            ORDER BY r.measured_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("since", since != null ? Timestamp.from(since) : null, java.sql.Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new TelemetryObservation(
                rs.getObject("sensor_id", UUID.class),
                rs.getString("type"),
                rs.getBigDecimal("value"),
                rs.getString("quality"),
                rs.getTimestamp("measured_at").toInstant()
        ));
    }

    @Override
    public Optional<WaterBalance> getWaterBalance(UUID fieldId) {
        if (fieldId == null) return Optional.empty();
        try {
            Map<UUID, FieldWaterSummaryData> map = waterConsumptionQueryRepository.getFieldWaterSummaries(
                    List.of(fieldId),
                    Instant.EPOCH,
                    Instant.now()
            );
            FieldWaterSummaryData data = map.get(fieldId);
            if (data != null && data.totalAllocatedVolumeM3() != null) {
                BigDecimal allocated = data.totalAllocatedVolumeM3();
                BigDecimal consumed = data.totalConsumedVolumeM3() != null ? data.totalConsumedVolumeM3() : BigDecimal.ZERO;
                boolean isOver = consumed.compareTo(allocated) > 0;
                BigDecimal remaining = isOver ? BigDecimal.ZERO : allocated.subtract(consumed);

                return Optional.of(new WaterBalance(
                        fieldId,
                        allocated,
                        consumed,
                        remaining,
                        BigDecimal.ZERO,
                        isOver
                ));
            }
        } catch (Exception e) {
            log.warn("Error fetching water balance for field {}: {}", fieldId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<UpcomingPlanInfo> getUpcomingPlans(UUID fieldId, Instant until) {
        if (fieldId == null) return Collections.emptyList();
        String sql = """
            SELECT id, field_id, planned_start_at, recommended_volume_m3, status,
                   is_blocked, blocked_reason
            FROM smart_irrigation_plans
            WHERE field_id = :fieldId
              AND status IN ('PROPOSED', 'APPROVED', 'SCHEDULED')
              AND (cast(:until as timestamp) IS NULL OR planned_start_at <= :until)
            ORDER BY planned_start_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("until", until != null ? Timestamp.from(until) : null, java.sql.Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new UpcomingPlanInfo(
                rs.getObject("id", UUID.class),
                rs.getObject("field_id", UUID.class),
                rs.getTimestamp("planned_start_at") != null ? rs.getTimestamp("planned_start_at").toInstant() : null,
                rs.getBigDecimal("recommended_volume_m3"),
                rs.getString("status"),
                rs.getBoolean("is_blocked"),
                rs.getString("blocked_reason")
        ));
    }

    @Override
    public List<HydraulicAnomalyInfo> getActiveHydraulicAnomalies(UUID fieldId) {
        if (fieldId == null) return Collections.emptyList();
        String sql = """
            SELECT id, field_id, anomaly_type, severity, detected_at,
                   (status = 'RESOLVED') AS is_resolved
            FROM anomalies
            WHERE field_id = :fieldId
              AND status IN ('DETECTED', 'ACTIVE', 'INVESTIGATING')
            ORDER BY detected_at DESC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource("fieldId", fieldId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new HydraulicAnomalyInfo(
                rs.getObject("id", UUID.class),
                rs.getObject("field_id", UUID.class),
                rs.getString("anomaly_type"),
                rs.getString("severity"),
                rs.getTimestamp("detected_at").toInstant(),
                rs.getBoolean("is_resolved")
        ));
    }

    @Override
    public List<SensorHealthSummary> getSensorHealthHistory(UUID fieldId, Instant since) {
        if (fieldId == null) return Collections.emptyList();
        String sql = """
            SELECT s.id AS sensor_id, s.field_id, s.name, s.status, s.last_seen_at,
                   COUNT(r.id) AS total_readings,
                   COUNT(CASE WHEN r.quality = 'VALID' THEN 1 END) AS valid_readings,
                   COUNT(CASE WHEN r.quality = 'SUSPECT' THEN 1 END) AS suspect_readings,
                   COUNT(CASE WHEN r.quality = 'INVALID' THEN 1 END) AS invalid_readings
            FROM sensor_devices s
            LEFT JOIN sensor_readings r ON r.sensor_id = s.id AND (cast(:since as timestamp) IS NULL OR r.measured_at >= :since)
            WHERE s.field_id = :fieldId
            GROUP BY s.id, s.field_id, s.name, s.status, s.last_seen_at
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("since", since != null ? Timestamp.from(since) : null, java.sql.Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String status = rs.getString("status");
            int offlinePeriods = "OFFLINE".equalsIgnoreCase(status) ? 1 : 0;
            return new SensorHealthSummary(
                    rs.getObject("sensor_id", UUID.class),
                    rs.getObject("field_id", UUID.class),
                    rs.getString("name"),
                    status,
                    rs.getTimestamp("last_seen_at") != null ? rs.getTimestamp("last_seen_at").toInstant() : null,
                    rs.getLong("total_readings"),
                    rs.getLong("valid_readings"),
                    rs.getLong("suspect_readings"),
                    rs.getLong("invalid_readings"),
                    0,
                    offlinePeriods
            );
        });
    }

    @Override
    public WeatherContext getWeatherContext(UUID fieldId) {
        try {
            WeatherSnapshot weather = weatherDataProvider.getWeatherForField(fieldId);
            BigDecimal et0 = (weather != null && evapotranspirationCalculator != null)
                    ? evapotranspirationCalculator.calculateReferenceET(weather, Instant.now())
                    : BigDecimal.valueOf(5.0);

            if (weather == null) {
                return new WeatherContext(BigDecimal.valueOf(28.0), BigDecimal.valueOf(35.0), et0, BigDecimal.ZERO);
            }

            return new WeatherContext(
                    weather.getTemperature(),
                    weather.getRelativeHumidity(),
                    et0 != null ? et0 : BigDecimal.valueOf(5.0),
                    weather.getPrecipitationAmountMm()
            );
        } catch (Exception e) {
            log.warn("Weather context fallback for field {}: {}", fieldId, e.getMessage());
            return new WeatherContext(BigDecimal.valueOf(28.0), BigDecimal.valueOf(35.0), BigDecimal.valueOf(5.0), BigDecimal.ZERO);
        }
    }

    @Override
    public List<TelemetryObservation> getMoistureObservationsInWindow(UUID fieldId, Instant windowStart, Instant windowEnd) {
        if (fieldId == null || windowStart == null || windowEnd == null) return Collections.emptyList();
        String sql = """
            SELECT r.sensor_id, r.type, r."value", r.quality, r.measured_at
            FROM sensor_readings r
            WHERE r.field_id = :fieldId
              AND r.type = 'SOIL_MOISTURE'
              AND r.measured_at >= :start
              AND r.measured_at <= :end
            ORDER BY r.measured_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("start", Timestamp.from(windowStart), Types.TIMESTAMP)
                .addValue("end", Timestamp.from(windowEnd), Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new TelemetryObservation(
                rs.getObject("sensor_id", UUID.class),
                rs.getString("type"),
                rs.getBigDecimal("value"),
                rs.getString("quality"),
                rs.getTimestamp("measured_at").toInstant()
        ));
    }

    @Override
    public List<IrrigationEventOutcome> getIrrigationEventsInWindow(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null || from == null || to == null) return Collections.emptyList();
        String sql = """
            SELECT id, field_id, started_at, ended_at, actual_volume_m3, status, notes
            FROM irrigation_events
            WHERE field_id = :fieldId
              AND started_at >= :from
              AND started_at <= :to
            ORDER BY started_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("from", Timestamp.from(from), Types.TIMESTAMP)
                .addValue("to", Timestamp.from(to), Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new IrrigationEventOutcome(
                rs.getObject("id", UUID.class),
                rs.getObject("field_id", UUID.class),
                rs.getTimestamp("started_at") != null ? rs.getTimestamp("started_at").toInstant() : null,
                rs.getTimestamp("ended_at") != null ? rs.getTimestamp("ended_at").toInstant() : null,
                rs.getBigDecimal("actual_volume_m3"),
                rs.getString("status"),
                rs.getString("notes")
        ));
    }

    @Override
    public List<SmartPlanOutcome> getSmartPlansInWindow(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null || from == null || to == null) return Collections.emptyList();
        String sql = """
            SELECT id, field_id, planned_start_at, recommended_volume_m3, status, is_blocked, blocked_reason
            FROM smart_irrigation_plans
            WHERE field_id = :fieldId
              AND planned_start_at >= :from
              AND planned_start_at <= :to
            ORDER BY planned_start_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("from", Timestamp.from(from), Types.TIMESTAMP)
                .addValue("to", Timestamp.from(to), Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new SmartPlanOutcome(
                rs.getObject("id", UUID.class),
                rs.getObject("field_id", UUID.class),
                rs.getTimestamp("planned_start_at") != null ? rs.getTimestamp("planned_start_at").toInstant() : null,
                rs.getBigDecimal("recommended_volume_m3"),
                rs.getString("status"),
                rs.getBoolean("is_blocked"),
                rs.getString("blocked_reason")
        ));
    }

    @Override
    public List<HydraulicAnomalyInfo> getHydraulicAnomaliesInWindow(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null || from == null || to == null) return Collections.emptyList();
        String sql = """
            SELECT id, field_id, anomaly_type, severity, detected_at, (status = 'RESOLVED') AS is_resolved
            FROM anomalies
            WHERE field_id = :fieldId
              AND detected_at >= :from
              AND detected_at <= :to
            ORDER BY detected_at ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("from", Timestamp.from(from), Types.TIMESTAMP)
                .addValue("to", Timestamp.from(to), Types.TIMESTAMP);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new HydraulicAnomalyInfo(
                rs.getObject("id", UUID.class),
                rs.getObject("field_id", UUID.class),
                rs.getString("anomaly_type"),
                rs.getString("severity"),
                rs.getTimestamp("detected_at").toInstant(),
                rs.getBoolean("is_resolved")
        ));
    }

    @Override
    public BigDecimal getActualWaterConsumptionInWindow(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null || from == null || to == null) return BigDecimal.ZERO;
        String sql = """
            SELECT COALESCE(SUM(actual_volume_m3), 0)
            FROM irrigation_events
            WHERE field_id = :fieldId
              AND status = 'COMPLETED'
              AND started_at >= :from
              AND started_at <= :to
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fieldId", fieldId)
                .addValue("from", Timestamp.from(from), Types.TIMESTAMP)
                .addValue("to", Timestamp.from(to), Types.TIMESTAMP);

        BigDecimal sum = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private FieldMetadata mapFieldMetadata(ResultSet rs) throws SQLException {
        return new FieldMetadata(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_id", UUID.class),
                rs.getString("name"),
                rs.getString("crop_type"),
                rs.getString("soil_type"),
                rs.getString("irrigation_method"),
                rs.getBigDecimal("area_hectares"),
                "ACTIVE".equalsIgnoreCase(rs.getString("status"))
        );
    }
}
