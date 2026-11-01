# Telemetry Aggregation Engine & Modality Policies (`docs/telemetry-aggregation.md`)

## 1. Aggregation Philosophy & Agronomic Foundations

In precision agriculture, mathematical aggregations must reflect the underlying physical and thermodynamic realities of the environment. Computing mathematical operations without regard to physical modality produces misleading data that can corrupt automated irrigation scheduling and crop models.

For instance:
- **Summing Temperature**: Adding temperatures recorded every hour over 24 hours yields $\approx 600^\circ\text{C}$, a completely meaningless metric.
- **Summing Soil Moisture**: Adding volumetric water percentages ($30\% + 32\% + 29\%$) yields $\approx 91\%$, which has no physical correspondence to soil pore saturation.
- **Averaging Rainfall Rate**: If 20 mm of rain falls during a 15-minute thunderstorm followed by 45 minutes of zero precipitation, averaging the hourly sensor rates dilutes the intensity. Instead, cumulative rainfall ($\Sigma$) during the hour is the crucial agronomic parameter.

To ensure physical and scientific rigor, YANGIARIQ AQUA AI enforces strict **Modality-Specific Aggregation Policies** in the domain layer (`com.aquaai.sensor.domain.analytics.AggregationPolicy`).

---

## 2. Modality Aggregation Matrix

| Sensor Type | Physical Metric | Primary Unit | Allowed Aggregations | Prohibited Aggregations | Agronomic Rationale |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `SOIL_MOISTURE` | Volumetric Soil Moisture | `%` | `AVG`, `MIN`, `MAX` | `SUM` | Intensive quantity. Reflects root zone available water. Average, minimum (dry down), and maximum (field capacity) are valid. Sum is physically meaningless. |
| `TEMPERATURE` | Ambient Air Temperature | `°C` | `AVG`, `MIN`, `MAX` | `SUM` | Intensive thermodynamic variable. Diurnal swing (`MIN` to `MAX`) and mean (`AVG`) determine growing degree days (GDD). Sum is meaningless. |
| `HUMIDITY` | Relative Air Humidity | `%` | `AVG`, `MIN`, `MAX` | `SUM` | Intensive ratio of vapor pressure. Indicates atmospheric vapor pressure deficit (VPD). Sum is meaningless. |
| `WATER_FLOW` | Instantaneous Discharge Rate | `m³/h` | `AVG`, `MIN`, `MAX` | `SUM` (raw) | Intensive rate of flow. Mean flow rate across an hour describes delivery consistency. Total volume delivery is calculated via time-integral ($Q \times \Delta t$), not simple addition of rates. |
| `WATER_LEVEL` | Canal / Borehole Depth | `m` | `AVG`, `MIN`, `MAX` | `SUM` | Physical surface elevation. Mean and extreme stage levels matter for canal overflow and pump suction safety. Sum is meaningless. |
| `PRESSURE` | Pipeline Hydraulic Pressure | `bar` | `AVG`, `MIN`, `MAX` | `SUM` | Intensive fluid pressure. Critical for leak detection (`MIN` drop) and burst protection (`MAX` spike). Sum is meaningless. |
| `SALINITY` | Electrical Conductivity ($EC_e$) | `dS/m` | `AVG`, `MIN`, `MAX` | `SUM` | Intensive ionic concentration. Evaluates soil/water salinization risk in Khorezm saline soils. Sum is meaningless. |
| `RAINFALL` | Precipitation Depth | `mm` | `SUM`, `MIN`, `MAX` | `AVG` | Extensive quantity. Total precipitation accumulated during an interval represents effective rainfall entering the soil water balance. Average is disallowed as it distorts event totals. |

---

## 3. Aggregation Intervals & Temporal Bucketing

Supported intervals are represented by the enum `com.aquaai.sensor.domain.analytics.AggregationInterval`:

```java
public enum AggregationInterval {
    FIVE_MINUTES("5m", 300, 2),
    FIFTEEN_MINUTES("15m", 900, 7),
    HOURLY("1h", 3600, 31),
    DAILY("1d", 86400, 365),
    WEEKLY("1w", 604800, 1825);
    // ...
}
```

### 3.1 UTC Boundary Truncation Formulas

All bucketing calculations are executed in UTC to prevent timezone skew, daylight saving anomalies, and leap second inconsistencies.

1. **Sub-Hour & Hourly Buckets (`5m`, `15m`, `1h`)**:
   Calculated using UTC epoch second modular arithmetic:
   $$\text{epochSeconds} = \text{instant.getEpochSecond()}$$
   $$\text{bucketStartEpoch} = \text{epochSeconds} - (\text{epochSeconds} \pmod{\text{durationSeconds}})$$
   $$\text{bucketStartTime} = \text{Instant.ofEpochSecond}(\text{bucketStartEpoch})$$
   $$\text{bucketEndTime} = \text{bucketStartTime.plusSeconds}(\text{durationSeconds})$$

2. **Daily Buckets (`1d`)**:
   Truncated to midnight UTC:
   $$\text{bucketStartTime} = \text{instant.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toInstant()}$$
   $$\text{bucketEndTime} = \text{bucketStartTime.plus(1, ChronoUnit.DAYS)}$$

3. **Weekly Buckets (`1w`)**:
   Aligned to ISO-8601 Monday at 00:00:00 UTC:
   $$\text{zonedDateTime} = \text{instant.atZone(ZoneOffset.UTC)}$$
   $$\text{bucketStartTime} = \text{zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))}$$
   $$\quad\quad\quad\quad\quad\text{.truncatedTo(ChronoUnit.DAYS).toInstant()}$$
   $$\text{bucketEndTime} = \text{bucketStartTime.plus(7, ChronoUnit.DAYS)}$$

---

## 4. Single-Pass Bucket Aggregation Engine

The domain port `SensorAnalyticsRepository` defines the aggregation contract:

```java
public interface SensorAnalyticsRepository {
    List<TelemetryAggregate> aggregateBySensor(
        UUID sensorId,
        SensorType sensorType,
        MeasurementUnit unit,
        AggregationInterval interval,
        Instant from,
        Instant to,
        ReadingQuality quality
    );

    List<TelemetryAggregate> aggregateByField(
        UUID fieldId,
        SensorType sensorType,
        MeasurementUnit unit,
        AggregationInterval interval,
        Instant from,
        Instant to,
        ReadingQuality quality
    );

    DataQualityMetrics computeDataQuality(
        UUID fieldId,
        Instant from,
        Instant to
    );

    SensorAvailabilitySummary computeAvailability(UUID fieldId);
}
```

### 4.1 Chronological Stream Processing Algorithm

In `SensorAnalyticsRepositoryImpl`, raw readings retrieved via `SensorReadingPointProjection` are already sorted by `measuredAt ASC`. The aggregation engine processes them in a single linear pass ($O(N)$ time complexity, $O(K)$ space where $K$ is the number of intervals):

```java
Instant currentBucketStart = null;
Instant currentBucketEnd = null;
BigDecimal currentMin = null;
BigDecimal currentMax = null;
BigDecimal currentSum = BigDecimal.ZERO;
BigDecimal firstVal = null;
Instant firstAt = null;
BigDecimal lastVal = null;
Instant lastAt = null;
int count = 0;

for (SensorReadingPointProjection p : points) {
    Instant bucketStart = interval.truncate(p.getMeasuredAt());
    
    // Check if reading belongs to a new time bucket
    if (currentBucketStart == null || !currentBucketStart.equals(bucketStart)) {
        if (currentBucketStart != null) {
            // Finalize previous bucket and apply AggregationPolicy
            result.add(buildAggregate(...));
        }
        // Initialize new bucket
        currentBucketStart = bucketStart;
        currentBucketEnd = interval.calculateBucketEnd(bucketStart);
        currentMin = p.getValue();
        currentMax = p.getValue();
        currentSum = p.getValue();
        firstVal = p.getValue();
        firstAt = p.getMeasuredAt();
        lastVal = p.getValue();
        lastAt = p.getMeasuredAt();
        count = 1;
    } else {
        // Accumulate within current bucket
        if (p.getValue().compareTo(currentMin) < 0) currentMin = p.getValue();
        if (p.getValue().compareTo(currentMax) > 0) currentMax = p.getValue();
        currentSum = currentSum.add(p.getValue());
        lastVal = p.getValue();
        lastAt = p.getMeasuredAt();
        count++;
    }
}
```

### 4.2 Numeric Precision & Rounding Rules

All calculated values retain fixed precision using `BigDecimal` scaled to 4 decimal places with `RoundingMode.HALF_UP`:

$$\text{average} = \text{currentSum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP)}$$

If `AggregationPolicy.isSumMeaningful(sensorType)` is false, `sum` is explicitly emitted as `null`.
If `AggregationPolicy.isAverageMeaningful(sensorType)` is false, `average` is explicitly emitted as `null`.

This guarantees that downstream API consumers, frontend charts, and AI advisors receive mathematically and physically sound aggregates.
