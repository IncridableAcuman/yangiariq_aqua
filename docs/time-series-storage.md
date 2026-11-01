# Time-Series Storage Architecture & TimescaleDB Strategy (`docs/time-series-storage.md`)

## 1. Architectural Strategy & Evaluation

Agricultural telemetry platforms capture continuous streams of time-stamped environmental and hydraulic observations. In YANGIARIQ AQUA AI, sensor readings measure soil moisture, canal and pipe water flows, ambient temperature, relative humidity, salinity, and rainfall across Yangiariq district.

### 1.1 TimescaleDB Evaluation
During the architectural design for Milestone 07.3, the project evaluated TimescaleDB as a dedicated time-series engine:

| Metric / Consideration | Native PostgreSQL 16 (Current Choice) | TimescaleDB Extension |
| :--- | :--- | :--- |
| **Deployment Footprint** | Standard Alpine PostgreSQL (`postgres:16-alpine`), zero extra dependencies | Requires custom Docker image or TimescaleDB HA build |
| **Extension Availability** | 100% available in all PostgreSQL environments | Not installed in standard cloud/distro PostgreSQL packages |
| **Migration Risk** | Zero. Retains existing Liquibase migration history | Requires schema rewrite into hypertables |
| **Query Performance (Small/Medium Scale)** | Sub-millisecond indexed queries via composite B-tree indexes | Comparable sub-millisecond execution |
| **Query Performance (High Scale: 100M+ rows)** | Requires table partitioning (`PARTITION BY RANGE (measured_at)`) | Automatic chunk management and native chunk-level compression |
| **Compatibility** | 100% compatible with Spring Data JPA and testcontainers | Compatible, but test containers require custom image |

### 1.2 Decision: PostgreSQL-Native Time-Series Architecture
Because the active deployment uses standard PostgreSQL 16 Alpine and must maintain stability without introducing fragile container dependencies, **Native PostgreSQL with High-Performance Composite Indexing & Projections** was adopted as the foundational architecture.

An automated hypertable migration path is documented and maintained so TimescaleDB can be enabled seamlessly when hardware scaling demands it.

---

## 2. Relational Schema & Storage Engine

The telemetry storage engine is defined in Liquibase changeset `016-create-sensor-domain-tables` (`008-create-sensor-domain-tables.sql`) and indexed in changeset `017-create-sensor-analytics-indexes` (`009-create-sensor-analytics-indexes.sql`).

### 2.1 Table: `sensor_devices`

Stores hardware device identity, field association, and real-time operational state.

```sql
CREATE TABLE sensor_devices (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL REFERENCES fields(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    device_identifier VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    provider VARCHAR(64) NOT NULL DEFAULT 'SIMULATED',
    description VARCHAR(500),
    installed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
```

### 2.2 Table: `sensor_readings`

Append-only, immutable time-series observation table.

```sql
CREATE TABLE sensor_readings (
    id UUID PRIMARY KEY,
    sensor_id UUID NOT NULL REFERENCES sensor_devices(id) ON DELETE CASCADE,
    field_id UUID NOT NULL REFERENCES fields(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    value NUMERIC(14, 4) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    unit_symbol VARCHAR(16) NOT NULL,
    quality VARCHAR(32) NOT NULL DEFAULT 'VALID',
    source VARCHAR(64) NOT NULL DEFAULT 'SIMULATED',
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### Key Column Design Choices
- `value NUMERIC(14, 4)`: Eliminates IEEE-754 floating point imprecision for flow and pressure calculations while preserving physical precision.
- `measured_at TIMESTAMP WITH TIME ZONE`: Stores measurement instant in UTC with microsecond fidelity.
- `quality VARCHAR(32)`: Stores data health status (`VALID`, `SUSPECT`, `INVALID`).

---

## 3. Indexing Strategy for Time-Range Aggregations

To support rapid historical filtering and aggregation across arbitrary time ranges, four specialized indexes were introduced:

```sql
-- Index 1: Single Sensor Historical & Analytics Queries
-- Accelerates GET /api/v1/sensors/{sensorId}/analytics and /history
CREATE INDEX idx_sensor_readings_sensor_qual_time
ON sensor_readings (sensor_id, quality, measured_at DESC);

-- Index 2: Parcel Modality Aggregation Queries
-- Accelerates GET /api/v1/fields/{fieldId}/analytics across multiple sensors
CREATE INDEX idx_sensor_readings_field_type_qual_time
ON sensor_readings (field_id, type, quality, measured_at DESC);

-- Index 3: Parcel Hardware Availability Checks
-- Accelerates GET /api/v1/fields/{fieldId}/analytics/summary availability counts
CREATE INDEX idx_sensor_devices_field_status
ON sensor_devices (field_id, status);

-- Index 4: Device Heartbeat & Unresponsive Checks
-- Accelerates system-wide communicating vs unresponsive sensor evaluations
CREATE INDEX idx_sensor_devices_last_seen
ON sensor_devices (last_seen_at);
```

### Query Execution Plan (Index Only / Bitmap Index Scan)
Because the composite indexes lead with `(sensor_id, quality, measured_at DESC)` and `(field_id, type, quality, measured_at DESC)`, PostgreSQL performs a range scan over the exact time slice without scanning unrelated sensors or modalities:

```text
Bitmap Heap Scan on sensor_readings
  Recheck Cond: ((sensor_id = $1) AND (quality = 'VALID') AND (measured_at >= $2) AND (measured_at <= $3))
  ->  Bitmap Index Scan on idx_sensor_readings_sensor_qual_time
        Index Cond: ((sensor_id = $1) AND (quality = 'VALID') AND (measured_at >= $2) AND (measured_at <= $3))
```

---

## 4. Ingestion-to-Query Pipeline & Spring Data Projection

To achieve maximum throughput and minimum GC overhead when aggregating large volumes of readings (e.g. 5,000 observations over 30 days):

1. **Lightweight Projection Interface**:
   ```java
   public interface SensorReadingPointProjection {
       Instant getMeasuredAt();
       BigDecimal getValue();
   }
   ```
2. **Selective JPQL Projection**:
   ```java
   @Query("SELECT r.measuredAt AS measuredAt, r.value AS value " +
          "FROM SensorReadingJpaEntity r " +
          "WHERE r.sensorId = :sensorId " +
          "AND (:quality IS NULL OR r.quality = :quality) " +
          "AND r.measuredAt >= :from AND r.measuredAt <= :to " +
          "ORDER BY r.measuredAt ASC")
   List<SensorReadingPointProjection> findReadingsForSensorAnalytics(...);
   ```
   Hibernate generates `SELECT r.measured_at, r.value FROM sensor_readings ...`, fetching **only 2 lightweight columns** instead of loading complete entities, UUIDs, strings, and associations into JPA persistence context.

3. **Bucket Aggregator Engine**:
   The Java-side aggregation engine (`SensorAnalyticsRepositoryImpl`) processes the sorted projection stream linearly:
   - Truncates `measuredAt` to bucket boundaries via UTC epoch math.
   - Computes `average`, `minimum`, `maximum`, `sum`, `count`, `firstReadingValue`, and `lastReadingValue` in a single pass ($O(N)$ time, $O(1)$ intermediate space).

---

## 5. Retention Policy & Data Lifecycle Management

Agricultural telemetry follows a natural lifecycle:
- **Raw Observations (0 - 90 Days)**: Retained at full granularity (every 5-15 minutes) for real-time operations, troubleshooting, and anomaly validation.
- **Aggregated Historical Data (90+ Days)**: Archived or summarized into hourly/daily aggregates; raw sub-minute noise is purged.

### 5.1 TelemetryRetentionService

`com.aquaai.sensor.application.service.TelemetryRetentionService` provides programmatic and scheduled retention management:

```java
@Service
@Transactional
public class TelemetryRetentionService {

    private final SensorReadingRepository readingRepository;

    public int purgeExpiredTelemetry(int retentionDays) {
        if (retentionDays < 1) {
            throw new IllegalArgumentException("Retention days must be at least 1");
        }
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        return readingRepository.deleteByMeasuredAtBefore(cutoff);
    }
}
```

Repository implementation leverages direct batch execution:
```sql
DELETE FROM sensor_readings WHERE measured_at < :cutoff;
```

---

## 6. TimescaleDB Seamless Migration Blueprint

If the platform is deployed to an infrastructure that supports TimescaleDB (e.g. `timescale/timescaledb-ha:pg16`), the following migration changeset converts the table into a hypertable with automatic 7-day chunking:

```sql
-- Liquibase Changeset: 018-convert-sensor-readings-to-hypertable
-- Preconditions: onFail=MARK_RAN, onError=HALT
-- Precondition: extension 'timescaledb' is installed

CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

SELECT create_hypertable(
    'sensor_readings',
    'measured_at',
    chunk_time_interval => INTERVAL '7 days',
    migrate_data => TRUE
);

-- Native TimescaleDB chunk compression policy (compress chunks older than 14 days)
ALTER TABLE sensor_readings SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'sensor_id, field_id, type',
    timescaledb.compress_orderby = 'measured_at DESC'
);

SELECT add_compression_policy('sensor_readings', INTERVAL '14 days');
```

Because the domain and application layers query through repository ports (`SensorAnalyticsRepository`), enabling TimescaleDB hypertables requires **zero changes** to Java application code or REST controllers.
